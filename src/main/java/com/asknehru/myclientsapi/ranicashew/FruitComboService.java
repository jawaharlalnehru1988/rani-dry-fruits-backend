package com.asknehru.myclientsapi.ranicashew;

import com.asknehru.myclientsapi.core.exception.ApiValidationException;
import com.asknehru.myclientsapi.core.exception.ResourceNotFoundException;
import com.asknehru.myclientsapi.core.media.MediaStorageService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FruitComboService {

    private final FruitComboRepository fruitComboRepository;
    private final ComboItemRepository comboItemRepository;
    private final FruitGalleryRepository fruitGalleryRepository;
    private final FruitGalleryImageRepository fruitGalleryImageRepository;
    private final MediaStorageService mediaStorageService;

    public FruitComboService(
        FruitComboRepository fruitComboRepository,
        ComboItemRepository comboItemRepository,
        FruitGalleryRepository fruitGalleryRepository,
        FruitGalleryImageRepository fruitGalleryImageRepository,
        MediaStorageService mediaStorageService
    ) {
        this.fruitComboRepository = fruitComboRepository;
        this.comboItemRepository = comboItemRepository;
        this.fruitGalleryRepository = fruitGalleryRepository;
        this.fruitGalleryImageRepository = fruitGalleryImageRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Transactional(readOnly = true)
    public List<FruitComboResponse> getAll(Boolean activeOnly) {
        List<FruitCombo> combos = Boolean.TRUE.equals(activeOnly)
            ? fruitComboRepository.findAllByIsActiveTrueOrderByCreatedAtDesc()
            : fruitComboRepository.findAllByOrderByCreatedAtDesc();

        if (combos.isEmpty()) {
            return List.of();
        }

        List<Long> comboIds = combos.stream().map(FruitCombo::getId).toList();
        List<ComboItem> allItems = comboItemRepository.findAllByComboIdInOrderByComboIdAscSortOrderAscIdAsc(comboIds);
        Map<Long, List<ComboItem>> itemsByComboId = allItems.stream()
            .collect(Collectors.groupingBy(item -> item.getCombo().getId()));

        // Collect all fruit IDs to fetch images and names in batch
        List<Long> fruitIds = allItems.stream()
            .map(item -> item.getFruit() != null ? item.getFruit().getId() : null)
            .filter(id -> id != null)
            .distinct()
            .toList();

        Map<Long, String> fruitImagesById = fetchFirstImageForFruits(fruitIds);

        return combos.stream()
            .map(combo -> toResponse(combo, itemsByComboId.getOrDefault(combo.getId(), List.of()), fruitImagesById))
            .toList();
    }

    @Transactional(readOnly = true)
    public FruitComboResponse getById(Long id) {
        FruitCombo combo = fruitComboRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Combo pack not found with ID: " + id));

        List<ComboItem> items = comboItemRepository.findAllByComboIdOrderBySortOrderAscIdAsc(combo.getId());
        List<Long> fruitIds = items.stream()
            .map(item -> item.getFruit() != null ? item.getFruit().getId() : null)
            .filter(fid -> fid != null)
            .distinct()
            .toList();

        Map<Long, String> fruitImagesById = fetchFirstImageForFruits(fruitIds);
        return toResponse(combo, items, fruitImagesById);
    }

    @Transactional
    public FruitComboResponse create(FruitComboWriteRequest request) {
        validateRequest(request);

        FruitCombo combo = new FruitCombo();
        applyRequestData(combo, request);
        FruitCombo savedCombo = fruitComboRepository.save(combo);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            saveComboItems(savedCombo, request.getItems());
        }

        return getById(savedCombo.getId());
    }

    @Transactional
    public FruitComboResponse update(Long id, FruitComboWriteRequest request) {
        validateRequest(request);

        FruitCombo combo = fruitComboRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Combo pack not found with ID: " + id));

        applyRequestData(combo, request);
        FruitCombo savedCombo = fruitComboRepository.save(combo);

        // Replace combo items if provided
        combo.getItems().clear();
        fruitComboRepository.flush();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            saveComboItems(savedCombo, request.getItems());
        }

        return getById(savedCombo.getId());
    }

    @Transactional
    public void delete(Long id) {
        FruitCombo combo = fruitComboRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Combo pack not found with ID: " + id));

        if (combo.getImageUrl() != null && combo.getImageUrl().startsWith("/media/")) {
            mediaStorageService.deleteImageByUrl(combo.getImageUrl());
        }

        fruitComboRepository.delete(combo);
    }

    private void validateRequest(FruitComboWriteRequest request) {
        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new ApiValidationException("Combo name is required");
        }
        BigDecimal price = request.getMrp() != null ? request.getMrp() : request.getOfferPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiValidationException("Valid combined price is required");
        }
    }

    private void applyRequestData(FruitCombo combo, FruitComboWriteRequest request) {
        combo.setName(request.getName().trim());
        combo.setDescription(request.getDescription());

        // Handle image upload from base64 or path
        if (request.getImageUrl() != null && request.getImageUrl().startsWith("data:image/")) {
            String oldImg = combo.getImageUrl();
            String storedPath = mediaStorageService.storeFruitDataImage(request.getImageUrl(), request.getName());
            combo.setImageUrl(storedPath);
            if (oldImg != null && oldImg.startsWith("/media/")) {
                mediaStorageService.deleteImageByUrl(oldImg);
            }
        } else if (request.getImageUrl() != null) {
            combo.setImageUrl(request.getImageUrl().trim());
        }

        combo.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Combined price is set as both MRP and offerPrice with 0 discount
        BigDecimal price = request.getMrp() != null ? request.getMrp() : request.getOfferPrice();
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        combo.setMrp(price);
        combo.setDiscountPercentage(BigDecimal.ZERO);
        combo.setOfferPrice(price);
    }

    private void saveComboItems(FruitCombo combo, List<FruitComboWriteRequest.ComboItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return;
        }

        List<Long> fruitIds = itemRequests.stream().map(FruitComboWriteRequest.ComboItemRequest::getFruitId).toList();
        Map<Long, FruitGallery> fruitsById = fruitGalleryRepository.findAllById(fruitIds).stream()
            .collect(Collectors.toMap(FruitGallery::getId, Function.identity()));

        int sortOrder = 1;
        for (FruitComboWriteRequest.ComboItemRequest itemReq : itemRequests) {
            FruitGallery fruit = fruitsById.get(itemReq.getFruitId());
            if (fruit == null) {
                continue;
            }

            ComboItem item = new ComboItem();
            item.setCombo(combo);
            item.setFruit(fruit);
            item.setWeight(itemReq.getWeight() != null ? itemReq.getWeight().trim() : "Standard");
            item.setItemMrp(itemReq.getItemMrp() != null ? itemReq.getItemMrp() : BigDecimal.ZERO);
            item.setIndividualDescription(itemReq.getIndividualDescription());
            item.setSortOrder(itemReq.getSortOrder() != null ? itemReq.getSortOrder() : sortOrder++);

            comboItemRepository.save(item);
        }
    }

    private Map<Long, String> fetchFirstImageForFruits(List<Long> fruitIds) {
        if (fruitIds == null || fruitIds.isEmpty()) {
            return Map.of();
        }
        List<FruitGalleryImage> images = fruitGalleryImageRepository.findAllByFruitIdInOrderByFruitIdAscIdAsc(fruitIds);
        Map<Long, String> result = new java.util.HashMap<>();
        for (FruitGalleryImage img : images) {
            if (img.getFruit() != null && !result.containsKey(img.getFruit().getId())) {
                result.put(img.getFruit().getId(), img.getImagePath());
            }
        }
        return result;
    }

    private FruitComboResponse toResponse(FruitCombo combo, List<ComboItem> items, Map<Long, String> fruitImagesById) {
        List<ComboItemDto> itemDtos = items.stream().map(item -> {
            FruitGallery fruit = item.getFruit();
            return ComboItemDto.builder()
                .id(item.getId())
                .fruitId(fruit != null ? fruit.getId() : null)
                .fruitName(fruit != null ? fruit.getName() : "Unknown Product")
                .fruitImage(fruit != null ? fruitImagesById.get(fruit.getId()) : null)
                .weight(item.getWeight())
                .itemMrp(item.getItemMrp())
                .individualDescription(item.getIndividualDescription())
                .sortOrder(item.getSortOrder())
                .build();
        }).toList();

        return FruitComboResponse.builder()
            .id(combo.getId())
            .name(combo.getName())
            .description(combo.getDescription())
            .imageUrl(combo.getImageUrl())
            .mrp(combo.getMrp())
            .discountPercentage(combo.getDiscountPercentage())
            .offerPrice(combo.getOfferPrice())
            .isActive(combo.getIsActive())
            .createdAt(combo.getCreatedAt())
            .updatedAt(combo.getUpdatedAt())
            .items(itemDtos)
            .build();
    }
}
