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

    @JsonProperty("weightPrices")
    private List<WeightPriceInput> weightPrices;

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

    public List<WeightPriceInput> getWeightPrices() {
        return weightPrices;
    }

    public void setWeightPrices(List<WeightPriceInput> weightPrices) {
        this.weightPrices = weightPrices;
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

    public static class WeightPriceInput {
        private String weight;
        private BigDecimal mrp;

        @JsonProperty("discountPercentage")
        private BigDecimal discountPercentage;

        @JsonProperty("offerPrice")
        private BigDecimal offerPrice;

        public WeightPriceInput() {}

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public BigDecimal getMrp() {
            return mrp;
        }

        public void setMrp(BigDecimal mrp) {
            this.mrp = mrp;
        }

        public BigDecimal getDiscountPercentage() {
            return discountPercentage;
        }

        public void setDiscountPercentage(BigDecimal discountPercentage) {
            this.discountPercentage = discountPercentage;
        }

        public BigDecimal getOfferPrice() {
            return offerPrice;
        }

        public void setOfferPrice(BigDecimal offerPrice) {
            this.offerPrice = offerPrice;
        }
    }
}
