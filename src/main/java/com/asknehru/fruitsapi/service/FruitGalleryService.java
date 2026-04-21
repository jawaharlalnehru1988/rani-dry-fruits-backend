package com.asknehru.fruitsapi.service;

import com.asknehru.fruitsapi.domain.FruitGallery;
import com.asknehru.fruitsapi.domain.FruitGalleryImage;
import com.asknehru.fruitsapi.dto.FruitGalleryResponse;
import com.asknehru.fruitsapi.dto.FruitGalleryWriteRequest;
import com.asknehru.fruitsapi.exception.ApiValidationException;
import com.asknehru.fruitsapi.exception.ResourceNotFoundException;
import com.asknehru.fruitsapi.repository.FruitGalleryImageRepository;
import com.asknehru.fruitsapi.repository.FruitGalleryRepository;
import java.math.BigDecimal;
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

    public FruitGalleryService(FruitGalleryRepository fruitGalleryRepository, FruitGalleryImageRepository fruitGalleryImageRepository) {
        this.fruitGalleryRepository = fruitGalleryRepository;
        this.fruitGalleryImageRepository = fruitGalleryImageRepository;
    }

    @Transactional(readOnly = true)
    public List<FruitGalleryResponse> getAll() {
        List<FruitGallery> fruits = fruitGalleryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        if (fruits.isEmpty()) {
            return List.of();
        }

        Map<Long, List<FruitGalleryImage>> imagesByFruitId = groupImagesByFruitId(
            fruitGalleryImageRepository.findAllByFruitIdInOrderByFruitIdAscIdAsc(
                fruits.stream().map(FruitGallery::getId).toList()
            )
        );

        return fruits.stream()
            .map(fruit -> toResponse(fruit, imagesByFruitId.getOrDefault(fruit.getId(), List.of())))
            .toList();
    }

    @Transactional(readOnly = true)
    public FruitGalleryResponse getById(Long id) {
        FruitGallery fruit = fruitGalleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fruit gallery not found with id: " + id));

        List<FruitGalleryImage> images = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
        return toResponse(fruit, images);
    }

    @Transactional
    public FruitGalleryResponse create(FruitGalleryWriteRequest request) {
        validateRequest(request, false);

        FruitGallery fruit = new FruitGallery();
        fruit.setName(request.getName().trim());
        fruit.setPrice(request.getPrice());
        fruit.setDiscountPercentage(request.getDiscountPercentage());
        fruit.setDescription(request.getDescription() == null ? null : request.getDescription().trim());

        fruit = fruitGalleryRepository.save(fruit);

        List<FruitGalleryImage> images = buildImages(fruit, request.getImagePath());
        fruitGalleryImageRepository.saveAll(images);

        return toResponse(fruit, images);
    }

    @Transactional
    public FruitGalleryResponse update(Long id, FruitGalleryWriteRequest request) {
        FruitGallery fruit = fruitGalleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fruit gallery not found with id: " + id));

        validateRequest(request, true);

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
        if (request.getImagePath() != null) {
            List<FruitGalleryImage> existing = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
            fruitGalleryImageRepository.deleteAll(existing);
            images = buildImages(fruit, request.getImagePath());
            fruitGalleryImageRepository.saveAll(images);
        } else {
            images = fruitGalleryImageRepository.findAllByFruitIdOrderByIdAsc(id);
        }

        return toResponse(fruit, images);
    }

    @Transactional
    public void delete(Long id) {
        FruitGallery fruit = fruitGalleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fruit gallery not found with id: " + id));
        fruitGalleryRepository.delete(fruit);
    }

    private FruitGalleryResponse toResponse(FruitGallery fruit, List<FruitGalleryImage> images) {
        FruitGalleryResponse response = new FruitGalleryResponse();
        response.setId(fruit.getId());
        response.setName(fruit.getName());
        response.setPrice(fruit.getPrice());
        response.setDiscountPercentage(fruit.getDiscountPercentage());
        response.setDescription(fruit.getDescription());
        response.setImagePath(images.stream().map(FruitGalleryImage::getImagePath).toList());
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

    private Map<Long, List<FruitGalleryImage>> groupImagesByFruitId(List<FruitGalleryImage> images) {
        Map<Long, List<FruitGalleryImage>> result = new HashMap<>();
        for (FruitGalleryImage image : images) {
            Long fruitId = image.getFruit().getId();
            result.computeIfAbsent(fruitId, ignored -> new ArrayList<>()).add(image);
        }
        return result;
    }

    private void validateRequest(FruitGalleryWriteRequest request, boolean partial) {
        Map<String, List<String>> errors = new HashMap<>();

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
            if (request.getImagePath() == null) {
                addError(errors, "imagePath", "This field is required.");
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

        if (request.getImagePath() != null) {
            List<String> cleanedPaths = request.getImagePath().stream()
                .filter(path -> path != null && !path.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

            if (cleanedPaths.isEmpty()) {
                addError(errors, "imagePath", "At least one image path is required.");
            }
            request.setImagePath(cleanedPaths);
        }

        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, ignored -> new ArrayList<>()).add(message);
    }
}
