package com.supplychain.wms.inventoryadjustment.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.common.domain.enums.AdjustmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_adjustments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustment extends BaseEntity {

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "bin_id", nullable = false)
    private Long binId;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false)
    private AdjustmentType adjustmentType;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "adjustment_date", nullable = false)
    private LocalDateTime adjustmentDate;
}
