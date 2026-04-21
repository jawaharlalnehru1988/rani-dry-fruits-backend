package com.asknehru.fruitsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FruitGalleryWriteRequest {

    private String name;
    private BigDecimal price;

    @JsonProperty("discountPercentage")
    private BigDecimal discountPercentage;

    private String description;

    @JsonProperty("imagePath")
    private List<String> imagePath;
}
