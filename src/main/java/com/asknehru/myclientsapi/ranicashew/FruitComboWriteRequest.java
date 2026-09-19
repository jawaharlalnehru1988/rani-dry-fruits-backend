package com.asknehru.myclientsapi.ranicashew;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FruitComboWriteRequest {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal mrp;
    private BigDecimal discountPercentage;
    private BigDecimal offerPrice;
    private Boolean isActive;
    private List<ComboItemRequest> items = new ArrayList<>();

    @Data
    public static class ComboItemRequest {
        private Long fruitId;
        private String weight;
        private BigDecimal itemMrp;
        private String individualDescription;
        private Integer sortOrder;
    }
}
