package com.supplychain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bin_transfers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinTransfer extends BaseEntity {

    // BaseEntity already provides: id, orgId, dcId, status, remarks,
    // createdBy, createdDate, modifiedBy, modifiedDate, deleted

    @Column(name = "source_warehouse_id", nullable = false)
    private Long sourceWarehouseId;

    @Column(name = "source_bin_id", nullable = false)
    private Long sourceBinId;

    @Column(name = "destination_warehouse_id", nullable = false)
    private Long destinationWarehouseId;

    @Column(name = "destination_bin_id", nullable = false)
    private Long destinationBinId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;
}
