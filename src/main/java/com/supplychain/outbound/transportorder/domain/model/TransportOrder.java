package com.supplychain.outbound.transportorder.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.outbound.transportorder.domain.enums.TransportOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transport_orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrder extends BaseEntity {

    // 1. Business key – backend-generated, never client-supplied
    @Column(name = "to_number", nullable = false)
    private String toNumber;

    // 2. Status
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private TransportOrderStatus toStatus;

    // 3. Transportation details
    @Column(name = "mode", nullable = false)
    private String mode;

    @Column(name = "service_level_name", nullable = false)
    private String serviceLevelName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // 4. Origin Location (FK + snapshot)
    @Column(name = "origin_location_id", nullable = false)
    private Long originLocationId;

    @Column(name = "origin_location_code")
    private String originLocationCode;

    @Column(name = "origin_location_name")
    private String originLocationName;

    // 5. Destination Location (FK + snapshot)
    @Column(name = "destination_location_id", nullable = false)
    private Long destinationLocationId;

    @Column(name = "destination_location_code")
    private String destinationLocationCode;

    @Column(name = "destination_location_name")
    private String destinationLocationName;

    // 6. Pickup Window
    @Column(name = "pick_up_from", nullable = false)
    private LocalDateTime pickUpFrom;

    @Column(name = "pick_up_to", nullable = false)
    private LocalDateTime pickUpTo;

    // 7. Delivery Window
    @Column(name = "delivery_from", nullable = false)
    private LocalDateTime deliveryFrom;

    @Column(name = "delivery_to", nullable = false)
    private LocalDateTime deliveryTo;

    // 8. Cross-reference strings (optional, denormalized snapshots — not FK constraints)
    //    poNumber  : customer's purchase order reference that originated this shipment
    //    asnNumber : outbound ASN reference sent to the consignee
    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "asn_number")
    private String asnNumber;

    // 9. Child line items
    @Builder.Default
    @OneToMany(mappedBy = "transportOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransportOrderLineItem> lineItems = new ArrayList<>();

    // 9. Outbound Order associations
    @Builder.Default
    @OneToMany(mappedBy = "transportOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransportOrderOutboundOrder> outboundOrderAssociations = new ArrayList<>();

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    public void addLineItem(TransportOrderLineItem item) {
        if (item != null) {
            item.setTransportOrder(this);
            this.lineItems.add(item);
        }
    }

    public void clearLineItems() {
        for (TransportOrderLineItem item : this.lineItems) {
            item.setTransportOrder(null);
        }
        this.lineItems.clear();
    }

    public void addOutboundOrderAssociation(TransportOrderOutboundOrder assoc) {
        if (assoc != null) {
            assoc.setTransportOrder(this);
            this.outboundOrderAssociations.add(assoc);
        }
    }

    public void clearOutboundOrderAssociations() {
        for (TransportOrderOutboundOrder assoc : this.outboundOrderAssociations) {
            assoc.setTransportOrder(null);
        }
        this.outboundOrderAssociations.clear();
    }
}
