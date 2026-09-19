package com.asknehru.myclientsapi.ranicashew;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboItemDto {
    private Long id;
    private Long fruitId;
    private String fruitName;
    private String fruitImage;
    private String weight;
    private BigDecimal itemMrp;
    private String individualDescription;
    private Integer sortOrder;
}
