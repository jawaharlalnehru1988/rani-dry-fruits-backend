package com.asknehru.myclientsapi.ranicashew;

import com.asknehru.myclientsapi.ranicashew.FruitGalleryWriteRequest.ImageInput;
import com.asknehru.myclientsapi.ranicashew.FruitGalleryWriteRequest.WeightPriceInput;
import com.asknehru.myclientsapi.core.exception.ApiValidationException;
import com.asknehru.myclientsapi.core.exception.ResourceNotFoundException;
import com.asknehru.myclientsapi.core.media.MediaStorageService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FruitGalleryService {

    private final FruitGalleryRepository fruitGalleryRepository;
    private final FruitGalleryImageRepository fruitGalleryImageRepository;
    private final FruitWeightPriceRepository fruitWeightPriceRepository;
    private final MediaStorageService mediaStorageService;

    public FruitGalleryService(
        FruitGalleryRepository fruitGalleryRepository,
        FruitGalleryImageRepository fruitGalleryImageRepository,
        FruitWeightPriceRepository fruitWeightPriceRepository,
        MediaStorageService mediaStorageService
    ) {
        this.fruitGalleryRepository = fruitGalleryRepository;
        this.fruitGalleryImageRepository = fruitGalleryImageRepository;
        this.fruitWeightPriceRepository = fruitWeightPriceRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Transactional(readOnly = true)
    public List<FruitGalleryResponse> getAll() {
        List<FruitGallery> fruits = fruitGalleryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        if (fruits.isEmpty()) {
            return List.of();
        }

        List<Long> fruitIds = fruits.stream().map(FruitGallery::getId).toList();

        Map<Long, List<FruitGalleryImage>> imagesByFruitId = groupImagesByFruitId(
            fruitGalleryImageRepository.findAllByFruitIdInOrderByFruitIdAscIdAsc(fruitIds)
        );

        Map<Long, List<FruitWeightPrice>> weightPricesByFruitId = groupWeightPricesByFruitId(
            fruitWeightPriceRepository.findAllByFruitIdInOrderByFruitIdAscIdAsc(fruitIds)
        );

        return fruits.stream()
            .map(fruit -> toResponse(
                fruit,
                imagesByFruitId.getOrDefault(fruit.getId(), List.of()),
                weightPricesByFruitId.getOrDefault(fruit.getId(), List.of())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public FruitGalleryResponse getById(Long id) {
        FruitGallery fruit = fruitGalleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fruit gallery not found with id: " + id));

        List<FruitGalleryImage> images = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
        List<FruitWeightPrice> weightPrices = fruitWeightPriceRepository.findAllByFruitIdOrderByIdAsc(id);
        return toResponse(fruit, images, weightPrices);
    }

    @Transactional
    public FruitGalleryResponse create(FruitGalleryWriteRequest request) {
        validateRequest(request, false);
        List<String> resolvedPaths = resolveIncomingImagePaths(request);

        FruitGallery fruit = new FruitGallery();
        fruit.setName(request.getName().trim());
        fruit.setPrice(request.getPrice());
        fruit.setDiscountPercentage(request.getDiscountPercentage());
        fruit.setDescription(request.getDescription() == null ? null : request.getDescription().trim());

        fruit = fruitGalleryRepository.save(fruit);

        List<FruitGalleryImage> images = buildImages(fruit, resolvedPaths);
        fruitGalleryImageRepository.saveAll(images);

        List<FruitWeightPrice> weightPrices = buildWeightPrices(fruit, request.getWeightPrices());
        if (weightPrices.isEmpty()) {
            weightPrices = buildDefaultWeightPrices(fruit);
        }
        fruitWeightPriceRepository.saveAll(weightPrices);

        return toResponse(fruit, images, weightPrices);
    }

    @Transactional
    public FruitGalleryResponse update(Long id, FruitGalleryWriteRequest request) {
        FruitGallery fruit = fruitGalleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fruit gallery not found with id: " + id));

        validateRequest(request, true);
        List<String> resolvedPaths = resolveIncomingImagePaths(request);

        if (request.getName() != null) {
            fruit.setName(request.getName().trim());
        }
        if (request.getPrice() != null) {
            fruit.setPrice(request.getPrice());
        }
        if (request.getDiscountPercentage() != null) {
            fruit.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getDescription() != null) {
            fruit.setDescription(request.getDescription().trim());
        }

        fruit = fruitGalleryRepository.save(fruit);

        List<FruitGalleryImage> images;
        if (shouldReplaceImages(request)) {
            List<FruitGalleryImage> existing = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
            for (FruitGalleryImage img : existing) {
                if (img.getImagePath() != null && !resolvedPaths.contains(img.getImagePath())) {
                    mediaStorageService.deleteImageByUrl(img.getImagePath());
                }
            }
            fruitGalleryImageRepository.deleteAll(existing);
            images = buildImages(fruit, resolvedPaths);
            fruitGalleryImageRepository.saveAll(images);
        } else {
            images = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
        }

        List<FruitWeightPrice> weightPrices;
        if (request.getWeightPrices() != null) {
            List<FruitWeightPrice> existingWp = fruitWeightPriceRepository.findAllByFruitIdOrderByIdAsc(id);
            fruitWeightPriceRepository.deleteAll(existingWp);
            weightPrices = buildWeightPrices(fruit, request.getWeightPrices());
            fruitWeightPriceRepository.saveAll(weightPrices);
        } else {
            weightPrices = fruitWeightPriceRepository.findAllByFruitIdOrderByIdAsc(id);
        }

        return toResponse(fruit, images, weightPrices);
    }

    @Transactional
    public void delete(Long id) {
        FruitGallery fruit = fruitGalleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fruit gallery not found with id: " + id));
        List<FruitGalleryImage> existing = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
        for (FruitGalleryImage img : existing) {
            if (img.getImagePath() != null) {
                mediaStorageService.deleteImageByUrl(img.getImagePath());
            }
        }
        fruitWeightPriceRepository.deleteAll(fruitWeightPriceRepository.findAllByFruitIdOrderByIdAsc(id));
        fruitGalleryRepository.delete(fruit);
    }

    private FruitGalleryResponse toResponse(FruitGallery fruit, List<FruitGalleryImage> images, List<FruitWeightPrice> weightPrices) {
        FruitGalleryResponse response = new FruitGalleryResponse();
        response.setId(fruit.getId());
        response.setName(fruit.getName());
        response.setPrice(fruit.getPrice());
        response.setDiscountPercentage(fruit.getDiscountPercentage());
        response.setDescription(fruit.getDescription());
        response.setImagePath(images.stream().map(FruitGalleryImage::getImagePath).toList());
        response.setImages(images.stream().map(img -> new FruitGalleryResponse.ImageDto(img.getId(), img.getImagePath())).toList());
        response.setWeightPrices(weightPrices.stream().map(wp -> new FruitGalleryResponse.WeightPriceDto(
            wp.getId(),
            wp.getWeight(),
            wp.getMrp(),
            wp.getDiscountPercentage(),
            wp.getOfferPrice()
        )).toList());
        response.setCreatedAt(fruit.getCreatedAt());
        response.setUpdatedAt(fruit.getUpdatedAt());
        return response;
    }

    private List<FruitGalleryImage> buildImages(FruitGallery fruit, Collection<String> imagePaths) {
        return imagePaths.stream()
            .map(path -> {
                FruitGalleryImage image = new FruitGalleryImage();
                image.setFruit(fruit);
                image.setImagePath(path.trim());
                return image;
            })
            .toList();
    }

    private List<FruitWeightPrice> buildWeightPrices(FruitGallery fruit, List<WeightPriceInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }
        return inputs.stream()
            .filter(input -> input != null && input.getWeight() != null && !input.getWeight().trim().isEmpty())
            .map(input -> {
                FruitWeightPrice wp = new FruitWeightPrice();
                wp.setFruit(fruit);
                wp.setWeight(input.getWeight().trim());
                BigDecimal mrp = input.getMrp() != null ? input.getMrp() : BigDecimal.ZERO;
                wp.setMrp(mrp);
                BigDecimal discount = input.getDiscountPercentage() != null ? input.getDiscountPercentage() : new BigDecimal("10.00");
                wp.setDiscountPercentage(discount);

                BigDecimal offerPrice = input.getOfferPrice();
                if (offerPrice == null) {
                    BigDecimal discountFactor = BigDecimal.ONE.subtract(discount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    offerPrice = mrp.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
                }
                wp.setOfferPrice(offerPrice);
                return wp;
            })
            .toList();
    }

    private List<FruitWeightPrice> buildDefaultWeightPrices(FruitGallery fruit) {
        BigDecimal basePrice = fruit.getPrice() != null ? fruit.getPrice() : BigDecimal.ZERO;
        BigDecimal discount = fruit.getDiscountPercentage() != null && fruit.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0
            ? fruit.getDiscountPercentage() : new BigDecimal("10.00");

        List<FruitWeightPrice> list = new ArrayList<>();
        String[] weights = {"250gm", "500gm", "1Kg"};
        BigDecimal[] multipliers = {BigDecimal.ONE, new BigDecimal("1.95"), new BigDecimal("3.8")};

        for (int i = 0; i < weights.length; i++) {
            FruitWeightPrice wp = new FruitWeightPrice();
            wp.setFruit(fruit);
            wp.setWeight(weights[i]);
            BigDecimal mrp = basePrice.multiply(multipliers[i]).setScale(2, RoundingMode.HALF_UP);
            wp.setMrp(mrp);
            wp.setDiscountPercentage(discount);
            BigDecimal discountFactor = BigDecimal.ONE.subtract(discount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            wp.setOfferPrice(mrp.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP));
            list.add(wp);
        }
        return list;
    }

    private Map<Long, List<FruitGalleryImage>> groupImagesByFruitId(List<FruitGalleryImage> images) {
        Map<Long, List<FruitGalleryImage>> result = new HashMap<>();
        for (FruitGalleryImage image : images) {
            Long fruitId = image.getFruit().getId();
            result.computeIfAbsent(fruitId, ignored -> new ArrayList<>()).add(image);
        }
        return result;
    }

    private Map<Long, List<FruitWeightPrice>> groupWeightPricesByFruitId(List<FruitWeightPrice> weightPrices) {
        Map<Long, List<FruitWeightPrice>> result = new HashMap<>();
        for (FruitWeightPrice wp : weightPrices) {
            Long fruitId = wp.getFruit().getId();
            result.computeIfAbsent(fruitId, ignored -> new ArrayList<>()).add(wp);
        }
        return result;
    }

    private void validateRequest(FruitGalleryWriteRequest request, boolean partial) {
        Map<String, List<String>> errors = new HashMap<>();

        // If price or discount percentage are not set on root request, derive them from the first weight variant if present
        if (request.getWeightPrices() != null && !request.getWeightPrices().isEmpty()) {
            WeightPriceInput first = request.getWeightPrices().get(0);
            if (request.getPrice() == null && first.getMrp() != null) {
                request.setPrice(first.getMrp());
            }
            if (request.getDiscountPercentage() == null && first.getDiscountPercentage() != null) {
                request.setDiscountPercentage(first.getDiscountPercentage());
            }
        }

        if (!partial) {
            if (request.getName() == null) {
                addError(errors, "name", "This field is required.");
            }
            if (request.getPrice() == null) {
                addError(errors, "price", "This field is required.");
            }
            if (request.getDiscountPercentage() == null) {
                addError(errors, "discountPercentage", "This field is required.");
            }
            if (request.getDescription() == null) {
                addError(errors, "description", "This field is required.");
            }
        }

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                addError(errors, "name", "This field may not be blank.");
            }
            if (name.length() > 200) {
                addError(errors, "name", "Ensure this field has no more than 200 characters.");
            }
        }

        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            addError(errors, "price", "Ensure this value is greater than or equal to 0.");
        }

        if (request.getDiscountPercentage() != null) {
            if (request.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0) {
                addError(errors, "discountPercentage", "Ensure this value is greater than or equal to 0.");
            }
            if (request.getDiscountPercentage().compareTo(new BigDecimal("100")) > 0) {
                addError(errors, "discountPercentage", "Ensure this value is less than or equal to 100.");
            }
        }

        List<String> cleanedPaths = null;
        if (request.getImagePath() != null) {
            cleanedPaths = request.getImagePath().stream()
                .filter(path -> path != null && !path.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
            request.setImagePath(cleanedPaths);
        }

        List<ImageInput> cleanedImages = null;
        if (request.getImages() != null) {
            cleanedImages = request.getImages().stream()
                .filter(image -> image != null && image.getImageUrl() != null && !image.getImageUrl().trim().isEmpty())
                .toList();
            request.setImages(cleanedImages);
        }

        boolean hasImagePath = cleanedPaths != null && !cleanedPaths.isEmpty();
        boolean hasImages = cleanedImages != null && !cleanedImages.isEmpty();

        if (!partial) {
            if (!hasImagePath && !hasImages) {
                addError(errors, "images", "At least one image or imageUrl is required.");
            }
        } else {
            boolean imagesFieldProvided = (request.getImagePath() != null || request.getImages() != null);
            if (imagesFieldProvided && !hasImagePath && !hasImages) {
                addError(errors, "images", "At least one image or imageUrl is required.");
            }
        }

        if (request.getWeightPrices() != null) {
            for (int i = 0; i < request.getWeightPrices().size(); i++) {
                WeightPriceInput wp = request.getWeightPrices().get(i);
                if (wp == null || wp.getWeight() == null || wp.getWeight().trim().isEmpty()) {
                    addError(errors, "weightPrices[" + i + "].weight", "Weight is required.");
                }
                if (wp != null && wp.getMrp() != null && wp.getMrp().compareTo(BigDecimal.ZERO) < 0) {
                    addError(errors, "weightPrices[" + i + "].mrp", "MRP must be greater than or equal to 0.");
                }
                if (wp != null && wp.getDiscountPercentage() != null) {
                    if (wp.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0 || wp.getDiscountPercentage().compareTo(new BigDecimal("100")) > 0) {
                        addError(errors, "weightPrices[" + i + "].discountPercentage", "Offer percentage must be between 0 and 100.");
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }
    }

    private List<String> resolveIncomingImagePaths(FruitGalleryWriteRequest request) {
        List<String> resolved = new ArrayList<>();

        if (request.getImagePath() != null) {
            resolved.addAll(request.getImagePath());
        }

        if (request.getImages() != null) {
            for (ImageInput image : request.getImages()) {
                resolved.add(mediaStorageService.storeFruitDataImage(image.getImageUrl(), request.getName()));
            }
        }

        return resolved;
    }

    private boolean shouldReplaceImages(FruitGalleryWriteRequest request) {
        boolean hasImagePath = request.getImagePath() != null && !request.getImagePath().isEmpty();
        boolean hasImages = request.getImages() != null && !request.getImages().isEmpty();
        return hasImagePath || hasImages;
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, ignored -> new ArrayList<>()).add(message);
    }
}
