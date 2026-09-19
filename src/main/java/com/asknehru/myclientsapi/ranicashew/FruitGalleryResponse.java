package com.asknehru.myclientsapi.ranicashew;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FruitGalleryResponse {

    private Long id;
    private String name;
    private BigDecimal price;

    @JsonProperty("discountPercentage")
    private BigDecimal discountPercentage;

    private String description;

    @JsonProperty("imagePath")
    private List<String> imagePath;

    @JsonProperty("images")
    private List<ImageDto> images;

    @JsonProperty("weightPrices")
    private List<WeightPriceDto> weightPrices;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    public static class ImageDto {
        private Long id;
        private String imageUrl;

        public ImageDto(Long id, String imageUrl) {
            this.id = id;
            this.imageUrl = imageUrl;
        }
    }

    @Data
    @NoArgsConstructor
    public static class WeightPriceDto {
        private Long id;
        private String weight;
        private BigDecimal mrp;

        @JsonProperty("discountPercentage")
        private BigDecimal discountPercentage;

        @JsonProperty("offerPrice")
        private BigDecimal offerPrice;

        public WeightPriceDto(Long id, String weight, BigDecimal mrp, BigDecimal discountPercentage, BigDecimal offerPrice) {
            this.id = id;
            this.weight = weight;
            this.mrp = mrp;
            this.discountPercentage = discountPercentage;
            this.offerPrice = offerPrice;
        }
    }
}
