package com.asknehru.myclientsapi.ranicashew;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FruitComboResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal mrp;
    private BigDecimal discountPercentage;
    private BigDecimal offerPrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<ComboItemDto> items = new ArrayList<>();
}
