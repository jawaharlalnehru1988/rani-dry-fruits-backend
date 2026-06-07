package com.asknehru.myclientsapi.ranicashew;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class FruitGalleryWriteRequest {

    private String name;
    private BigDecimal price;

    @JsonProperty("discountPercentage")
    private BigDecimal discountPercentage;

    private String description;

    @JsonProperty("imagePath")
    private List<String> imagePath;

    private List<ImageInput> images;

    public FruitGalleryWriteRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImagePath() {
        return imagePath;
    }

    public void setImagePath(List<String> imagePath) {
        this.imagePath = imagePath;
    }

    public List<ImageInput> getImages() {
        return images;
    }

    public void setImages(List<ImageInput> images) {
        this.images = images;
    }

    public static class ImageInput {
        @JsonProperty("imageUrl")
        private String imageUrl;

        public ImageInput() {}

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
