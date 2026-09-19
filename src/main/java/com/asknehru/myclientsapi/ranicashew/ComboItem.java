package com.asknehru.myclientsapi.ranicashew;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "combo_items")
public class ComboItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "combo_id", nullable = false)
    @JsonIgnore
    private FruitCombo combo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id")
    private FruitGallery fruit;

    @Column(name = "weight", nullable = false, length = 50)
    private String weight;

    @Column(name = "item_mrp", precision = 10, scale = 2)
    private BigDecimal itemMrp = BigDecimal.ZERO;

    @Column(name = "individual_description")
    private String individualDescription;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
