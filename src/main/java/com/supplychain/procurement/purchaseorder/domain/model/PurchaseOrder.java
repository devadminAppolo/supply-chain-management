package com.supplychain.procurement.purchaseorder.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.purchaseorder.domain.enums.FreightTerms;
import com.supplychain.procurement.purchaseorder.domain.enums.Incoterms;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.PoType;
import com.supplychain.procurement.purchaseorder.domain.enums.Priority;
import com.supplychain.procurement.purchaseorder.domain.enums.ShippingMethod;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder extends BaseEntity {

    // 1. PO Number
    @Column(name = "po_number", nullable = false)
    private String poNumber;

    // 2. PO Type
    @Enumerated(EnumType.STRING)
    @Column(name = "po_type", nullable = false)
    private PoType poType;

    // 3. PO Status
    @Enumerated(EnumType.STRING)
    @Column(name = "po_status", nullable = false)
    private PoStatus poStatus;

    // 4. PO Date
    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;

    // 5. Supplier
    @Column(name = "supplier", nullable = false)
    private String supplier;

    // 6. Supplier Reference Number
    @Column(name = "supplier_reference_number")
    private String supplierReferenceNumber;

    // 7. Supplier Quotation Number
    @Column(name = "supplier_quotation_number")
    private String supplierQuotationNumber;

    // 8. Company / Organization
    @Column(name = "company")
    private String company;

    // 9. Purchasing Organization
    @Column(name = "purchasing_organization")
    private String purchasingOrganization;

    // 10. Purchasing Group
    @Column(name = "purchasing_group")
    private String purchasingGroup;

    // 11. Distribution Center
    @Column(name = "distribution_center")
    private String distributionCenter;

    // 12. Warehouse / Delivery Location
    @Column(name = "warehouse")
    private String warehouse;

    // 13. Requested Delivery Date
    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;

    // 14. Promised Delivery Date
    @Column(name = "promised_delivery_date")
    private LocalDate promisedDeliveryDate;

    // 15. Latest Delivery Date
    @Column(name = "latest_delivery_date")
    private LocalDate latestDeliveryDate;

    // 16. Priority
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    // 17. Currency Code
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    // 18. Exchange Rate
    @Column(name = "exchange_rate", precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    // 19. Exchange Rate Fixed
    @Builder.Default
    @Column(name = "exchange_rate_fixed")
    private Boolean exchangeRateFixed = false;

    // 20. Payment Terms
    @Column(name = "payment_terms")
    private String paymentTerms;

    // 21. Net Payment Days
    @Column(name = "net_payment_days")
    private Integer netPaymentDays;

    // 22. Cash Discount 1 Days
    @Column(name = "cash_discount1_days")
    private Integer cashDiscount1Days;

    // 23. Cash Discount 1 %
    @Column(name = "cash_discount1_percent", precision = 7, scale = 4)
    private BigDecimal cashDiscount1Percent;

    // 24. Cash Discount 2 Days
    @Column(name = "cash_discount2_days")
    private Integer cashDiscount2Days;

    // 25. Cash Discount 2 %
    @Column(name = "cash_discount2_percent", precision = 7, scale = 4)
    private BigDecimal cashDiscount2Percent;

    // 26. Incoterms
    @Enumerated(EnumType.STRING)
    @Column(name = "incoterms")
    private Incoterms incoterms;

    // 27. Incoterms Version
    @Column(name = "incoterms_version")
    private String incotermsVersion;

    // 28. Incoterms Location
    @Column(name = "incoterms_location")
    private String incotermsLocation;

    // 29. Shipping Method
    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method")
    private ShippingMethod shippingMethod;

    // 30. Freight Terms
    @Enumerated(EnumType.STRING)
    @Column(name = "freight_terms")
    private FreightTerms freightTerms;

    // 31. Carrier
    @Column(name = "carrier")
    private String carrier;

    // 32. Appointment Required
    @Builder.Default
    @Column(name = "appointment_required")
    private Boolean appointmentRequired = false;

    // 33. Tax Code
    @Column(name = "tax_code")
    private String taxCode;

    // 34. Tax Country
    @Column(name = "tax_country")
    private String taxCountry;

    // 35. Subtotal Amount
    @Column(name = "subtotal_amount", precision = 19, scale = 4)
    private BigDecimal subtotalAmount;

    // 36. Discount Amount
    @Column(name = "discount_amount", precision = 19, scale = 4)
    private BigDecimal discountAmount;

    // 37. Freight Amount
    @Column(name = "freight_amount", precision = 19, scale = 4)
    private BigDecimal freightAmount;

    // 38. Tax Amount
    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount;

    // 39. Total Amount
    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    // 40. Notes / Instructions
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Child Line Items
    @Builder.Default
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderLineItem> lineItems = new ArrayList<>();

    public void addLineItem(PurchaseOrderLineItem item) {
        if (item != null) {
            item.setPurchaseOrder(this);
            this.lineItems.add(item);
        }
    }

    public void removeLineItem(PurchaseOrderLineItem item) {
        if (item != null) {
            item.setPurchaseOrder(null);
            this.lineItems.remove(item);
        }
    }

    public void clearLineItems() {
        for (PurchaseOrderLineItem item : this.lineItems) {
            item.setPurchaseOrder(null);
        }
        this.lineItems.clear();
    }
}
