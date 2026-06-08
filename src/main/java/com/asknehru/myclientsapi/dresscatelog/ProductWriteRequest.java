package com.asknehru.myclientsapi.dresscatelog;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductWriteRequest {
    private String name;
    private String category;
    private String imageUrl;
    private String prodDesc;
    private Boolean isNew;
    private List<SizeWisePriceInput> sizeWisePrice;

    @Data
    public static class SizeWisePriceInput {
        private String size;
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
    }
}
