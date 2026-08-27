package com.supplychain.procurement.asn.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.asn.domain.enums.AsnStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.Priority;
import com.supplychain.procurement.purchaseorder.domain.enums.ShippingMethod;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "advance_shipping_notices")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceShippingNotice extends BaseEntity {

    @Column(name = "asn_number", nullable = false)
    private String asnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "supplier_id")
    private String supplierId;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "warehouse_id")
    private String warehouseId;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column(name = "shipment_date", nullable = false)
    private LocalDate shipmentDate;

    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;

    @Column(name = "carrier_id")
    private String carrierId;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method")
    private ShippingMethod shippingMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;

    @Column(name = "packing_slip_number")
    private String packingSlipNumber;

    @Column(name = "bill_of_lading_number")
    private String billOfLadingNumber;

    @Column(name = "container_number")
    private String containerNumber;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "total_weight", precision = 19, scale = 4)
    private BigDecimal totalWeight;

    @Column(name = "weight_uom")
    private String weightUom;

    @Column(name = "total_volume", precision = 19, scale = 4)
    private BigDecimal totalVolume;

    @Column(name = "volume_uom")
    private String volumeUom;

    @Enumerated(EnumType.STRING)
    @Column(name = "asn_status", nullable = false)
    private AsnStatus asnStatus;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "advanceShippingNotice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvanceShippingNoticeLine> lines = new ArrayList<>();

    public void addLine(AdvanceShippingNoticeLine line) {
        if (line != null) {
            line.setAdvanceShippingNotice(this);
            this.lines.add(line);
        }
    }

    public void removeLine(AdvanceShippingNoticeLine line) {
        if (line != null) {
            line.setAdvanceShippingNotice(null);
            this.lines.remove(line);
        }
    }

    public void clearLines() {
        for (AdvanceShippingNoticeLine line : this.lines) {
            line.setAdvanceShippingNotice(null);
        }
        this.lines.clear();
    }
}
