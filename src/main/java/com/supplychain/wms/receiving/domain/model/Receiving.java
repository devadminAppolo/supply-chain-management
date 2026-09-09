package com.supplychain.wms.receiving.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.receiving.domain.enums.ReceivingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receivings")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Receiving extends BaseEntity {

    @Column(name = "receiving_number", nullable = false)
    private String receivingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private AdvanceShippingNotice advanceShippingNotice;

    @Column(name = "asn_number")
    private String asnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "purchase_order_number")
    private String purchaseOrderNumber;

    @Column(name = "supplier_id")
    private String supplierId;

    @Column(name = "supplier_name")
    private String supplierName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "warehouse_code")
    private String warehouseCode;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "receiving_date", nullable = false)
    private LocalDateTime receivingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiving_status", nullable = false)
    private ReceivingStatus receivingStatus;

    @Column(name = "notes")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "receiving", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReceivingPallet> pallets = new ArrayList<>();

    public void addPallet(ReceivingPallet pallet) {
        pallets.add(pallet);
        pallet.setReceiving(this);
    }

    public void removePallet(ReceivingPallet pallet) {
        pallets.remove(pallet);
        pallet.setReceiving(null);
    }
}
