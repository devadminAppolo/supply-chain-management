package com.supplychain.outbound.outboundorder.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "outbound_orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrder extends BaseEntity {

    // 1. Order Number – backend-generated, never supplied by the client
    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    // 2. Order Date
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    // 3. Order Type
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OutboundOrderType orderType;

    // 4. Order Status
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OutboundOrderStatus orderStatus;

    // 5. Customer (denormalized – no separate customer master in this WMS)
    @Column(name = "customer_code", nullable = false)
    private String customerCode;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    // 6. Origin Location (FK to locations table)
    @Column(name = "origin_location_id", nullable = false)
    private Long originLocationId;

    @Column(name = "origin_location_code")
    private String originLocationCode;

    @Column(name = "origin_location_name")
    private String originLocationName;

    // 7. Destination Location (FK to locations table)
    @Column(name = "destination_location_id", nullable = false)
    private Long destinationLocationId;

    @Column(name = "destination_location_code")
    private String destinationLocationCode;

    @Column(name = "destination_location_name")
    private String destinationLocationName;

    // 8. Pick Time Window
    @Column(name = "pick_time_window_start")
    private LocalDateTime pickTimeWindowStart;

    @Column(name = "pick_time_window_end")
    private LocalDateTime pickTimeWindowEnd;

    // 9. Delivery Time Window
    @Column(name = "delivery_time_window_start")
    private LocalDateTime deliveryTimeWindowStart;

    @Column(name = "delivery_time_window_end")
    private LocalDateTime deliveryTimeWindowEnd;

    // 10. Financials – backend-calculated
    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency_code")
    private String currencyCode;

    // 11. Instructions / Notes
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // -------------------------------------------------------
    // Child Lines
    // -------------------------------------------------------
    @Builder.Default
    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderLine> lines = new ArrayList<>();

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    public void addLine(OutboundOrderLine line) {
        if (line != null) {
            line.setOutboundOrder(this);
            this.lines.add(line);
        }
    }

    public void removeLine(OutboundOrderLine line) {
        if (line != null) {
            line.setOutboundOrder(null);
            this.lines.remove(line);
        }
    }

    public void clearLines() {
        for (OutboundOrderLine line : this.lines) {
            line.setOutboundOrder(null);
        }
        this.lines.clear();
    }
}
