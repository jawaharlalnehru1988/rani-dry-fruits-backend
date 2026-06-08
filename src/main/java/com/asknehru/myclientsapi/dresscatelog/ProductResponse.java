package com.asknehru.myclientsapi.dresscatelog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private String imageUrl;
    private String prodDesc;
    private Boolean isNew;
    private List<SizeWisePriceDto> sizeWisePrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class SizeWisePriceDto {
        private String size;
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
    }
}
