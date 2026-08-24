package com.supplychain.procurement.purchaseorder.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.procurement.purchaseorder.domain.enums.FreightTerms;
import com.supplychain.procurement.purchaseorder.domain.enums.Incoterms;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.PoType;
import com.supplychain.procurement.purchaseorder.domain.enums.Priority;
import com.supplychain.procurement.purchaseorder.domain.enums.ShippingMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderDto extends BaseDto {

    // 1. PO Number
    @NotBlank(message = "PO number is required")
    private String poNumber;

    // 2. PO Type
    @NotNull(message = "PO type is required")
    private PoType poType;

    // 3. PO Status
    @NotNull(message = "PO status is required")
    private PoStatus poStatus;

    // 4. PO Date
    @NotNull(message = "PO date is required")
    private LocalDate poDate;

    // 5. Supplier
    @NotBlank(message = "Supplier is required")
    private String supplier;

    // 6. Supplier Reference Number
    private String supplierReferenceNumber;

    // 7. Supplier Quotation Number
    private String supplierQuotationNumber;

    // 8. Company / Organization
    private String company;

    // 9. Purchasing Organization
    private String purchasingOrganization;

    // 10. Purchasing Group
    private String purchasingGroup;

    // 11. Distribution Center
    private String distributionCenter;

    // 12. Warehouse / Delivery Location
    private String warehouse;

    // 13. Requested Delivery Date
    private LocalDate requestedDeliveryDate;

    // 14. Promised Delivery Date
    private LocalDate promisedDeliveryDate;

    // 15. Latest Delivery Date
    private LocalDate latestDeliveryDate;

    // 16. Priority
    @NotNull(message = "Priority is required")
    private Priority priority;

    // 17. Currency Code
    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    // 18. Exchange Rate
    private BigDecimal exchangeRate;

    // 19. Exchange Rate Fixed
    private Boolean exchangeRateFixed = false;

    // 20. Payment Terms
    private String paymentTerms;

    // 21. Net Payment Days
    private Integer netPaymentDays;

    // 22. Cash Discount 1 Days
    private Integer cashDiscount1Days;

    // 23. Cash Discount 1 %
    private BigDecimal cashDiscount1Percent;

    // 24. Cash Discount 2 Days
    private Integer cashDiscount2Days;

    // 25. Cash Discount 2 %
    private BigDecimal cashDiscount2Percent;

    // 26. Incoterms
    private Incoterms incoterms;

    // 27. Incoterms Version
    private String incotermsVersion;

    // 28. Incoterms Location
    private String incotermsLocation;

    // 29. Shipping Method
    private ShippingMethod shippingMethod;

    // 30. Freight Terms
    private FreightTerms freightTerms;

    // 31. Carrier
    private String carrier;

    // 32. Appointment Required
    private Boolean appointmentRequired = false;

    // 33. Tax Code
    private String taxCode;

    // 34. Tax Country
    private String taxCountry;

    // 35. Subtotal Amount
    private BigDecimal subtotalAmount;

    // 36. Discount Amount
    private BigDecimal discountAmount;

    // 37. Freight Amount
    private BigDecimal freightAmount;

    // 38. Tax Amount
    private BigDecimal taxAmount;

    // 39. Total Amount
    private BigDecimal totalAmount;

    // 40. Notes / Instructions
    private String notes;

    // Child Line Items
    @Valid
    @NotEmpty(message = "Purchase order must contain at least one line item")
    private List<PurchaseOrderLineItemDto> lineItems = new ArrayList<>();
}
