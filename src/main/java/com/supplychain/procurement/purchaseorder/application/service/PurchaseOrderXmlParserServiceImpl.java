package com.supplychain.procurement.purchaseorder.application.service;

import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderDto;
import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderLineItemDto;
import com.supplychain.procurement.purchaseorder.domain.enums.Incoterms;
import com.supplychain.procurement.purchaseorder.domain.enums.LineStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.LineType;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.PoType;
import com.supplychain.procurement.purchaseorder.domain.enums.Priority;
import com.supplychain.procurement.purchaseorder.domain.enums.ProductClass;
import com.supplychain.procurement.purchaseorder.domain.enums.ProtectionLevelCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PurchaseOrderXmlParserServiceImpl implements PurchaseOrderXmlParserService {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public PurchaseOrderDto parseXml(InputStream inputStream) {
        if (inputStream == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XML stream cannot be null");
        }
        try {
            byte[] bytes = inputStream.readAllBytes();
            String rawContent = new String(bytes, StandardCharsets.UTF_8);
            return parseXml(rawContent);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to read Purchase Order XML input stream: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid XML input stream: " + e.getMessage(), e);
        }
    }

    @Override
    public PurchaseOrderDto parseXml(String xmlContent) {
        if (xmlContent == null || xmlContent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XML content cannot be empty");
        }

        String sanitized = sanitizeXml(xmlContent);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            // XXE and secure processing configuration
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(sanitized)));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            return mapOrderDocument(root);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Purchase Order XML: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid XML document: " + e.getMessage(), e);
        }
    }

    private String sanitizeXml(String xml) {
        if (xml == null) return "";

        // Strip UTF-8 Byte Order Mark (BOM) if present
        if (xml.startsWith("\uFEFF")) {
            xml = xml.substring(1);
        }
        xml = xml.trim();

        // Strip any browser copy-paste preamble (e.g. "This XML file does not appear...")
        int xmlDeclIndex = xml.indexOf("<?xml");
        int firstTagIndex = xml.indexOf('<');

        if (xmlDeclIndex >= 0) {
            xml = xml.substring(xmlDeclIndex);
        } else if (firstTagIndex >= 0) {
            xml = xml.substring(firstTagIndex);
        }

        return xml.trim();
    }

    private PurchaseOrderDto mapOrderDocument(Element root) {
        PurchaseOrderDto dto = new PurchaseOrderDto();

        // 1. PO Number
        String poNumber = getFirstChildTextByLocalName(root, "ID");
        if (poNumber == null || poNumber.isBlank()) {
            poNumber = "PO-" + System.currentTimeMillis();
        }
        dto.setPoNumber(poNumber);

        // 2. PO Date
        String issueDateStr = getFirstChildTextByLocalName(root, "IssueDate");
        LocalDate poDate = parseDateSafely(issueDateStr, LocalDate.now());
        dto.setPoDate(poDate);

        // 3. Currency Code
        String currencyCode = getFirstChildTextByLocalName(root, "DocumentCurrencyCode");
        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = "USD";
        }
        dto.setCurrencyCode(currencyCode.toUpperCase());

        // 4. Notes
        String note = getFirstChildTextByLocalName(root, "Note");
        dto.setNotes(note);

        // 5. Contract / PO Type
        Element contractElem = getFirstChildElementByLocalName(root, "Contract");
        if (contractElem != null) {
            String contractType = getFirstChildTextByLocalName(contractElem, "ContractType");
            dto.setPoType(mapPoType(contractType));
        } else {
            dto.setPoType(PoType.STANDARD);
        }

        // 6. Quotation Reference
        Element quoteElem = getFirstChildElementByLocalName(root, "QuotationDocumentReference");
        if (quoteElem != null) {
            dto.setSupplierQuotationNumber(getFirstChildTextByLocalName(quoteElem, "ID"));
        }

        // 7. Buyer / Company
        Element buyerPartyElem = getFirstChildElementByLocalName(root, "BuyerCustomerParty");
        if (buyerPartyElem != null) {
            Element party = getFirstChildElementByLocalName(buyerPartyElem, "Party");
            if (party != null) {
                Element partyName = getFirstChildElementByLocalName(party, "PartyName");
                if (partyName != null) {
                    dto.setCompany(getFirstChildTextByLocalName(partyName, "Name"));
                }
                Element postalAddress = getFirstChildElementByLocalName(party, "PostalAddress");
                if (postalAddress != null) {
                    dto.setPurchasingOrganization(getFirstChildTextByLocalName(postalAddress, "Department"));
                    Element country = getFirstChildElementByLocalName(postalAddress, "Country");
                    if (country != null) {
                        dto.setTaxCountry(getFirstChildTextByLocalName(country, "IdentificationCode"));
                    }
                }
                Element partyTaxScheme = getFirstChildElementByLocalName(party, "PartyTaxScheme");
                if (partyTaxScheme != null) {
                    Element taxScheme = getFirstChildElementByLocalName(partyTaxScheme, "TaxScheme");
                    if (taxScheme != null) {
                        dto.setTaxCode(getFirstChildTextByLocalName(taxScheme, "ID"));
                    }
                    if (dto.getTaxCountry() == null) {
                        Element regAddr = getFirstChildElementByLocalName(partyTaxScheme, "RegistrationAddress");
                        if (regAddr != null) {
                            Element country = getFirstChildElementByLocalName(regAddr, "Country");
                            if (country != null) {
                                dto.setTaxCountry(getFirstChildTextByLocalName(country, "IdentificationCode"));
                            }
                        }
                    }
                }
            }
        }

        // 8. Supplier
        Element sellerPartyElem = getFirstChildElementByLocalName(root, "SellerSupplierParty");
        if (sellerPartyElem != null) {
            Element party = getFirstChildElementByLocalName(sellerPartyElem, "Party");
            if (party != null) {
                Element partyName = getFirstChildElementByLocalName(party, "PartyName");
                if (partyName != null) {
                    dto.setSupplier(getFirstChildTextByLocalName(partyName, "Name"));
                }
                Element partyId = getFirstChildElementByLocalName(party, "PartyIdentification");
                if (partyId != null) {
                    dto.setSupplierReferenceNumber(getFirstChildTextByLocalName(partyId, "ID"));
                }
            }
        }
        if (dto.getSupplier() == null || dto.getSupplier().isBlank()) {
            dto.setSupplier("Default Supplier");
        }

        // 9. Delivery Details
        Element deliveryElem = getFirstChildElementByLocalName(root, "Delivery");
        if (deliveryElem != null) {
            Element reqPeriod = getFirstChildElementByLocalName(deliveryElem, "RequestedDeliveryPeriod");
            if (reqPeriod != null) {
                String startDateStr = getFirstChildTextByLocalName(reqPeriod, "StartDate");
                String endDateStr = getFirstChildTextByLocalName(reqPeriod, "EndDate");

                LocalDate startDate = parseDateSafely(startDateStr, null);
                LocalDate endDate = parseDateSafely(endDateStr, null);

                dto.setRequestedDeliveryDate(startDate);
                dto.setPromisedDeliveryDate(startDate != null ? startDate : endDate);
                dto.setLatestDeliveryDate(endDate != null ? endDate : startDate);
            }

            Element deliveryLocation = getFirstChildElementByLocalName(deliveryElem, "DeliveryLocation");
            if (deliveryLocation != null) {
                Element addr = getFirstChildElementByLocalName(deliveryLocation, "Address");
                if (addr != null) {
                    String street = getFirstChildTextByLocalName(addr, "StreetName");
                    String bldNo = getFirstChildTextByLocalName(addr, "BuildingNumber");
                    String city = getFirstChildTextByLocalName(addr, "CityName");
                    String locationStr = buildLocationString(street, bldNo, city);
                    dto.setWarehouse(locationStr);
                    dto.setDistributionCenter(city != null ? city : locationStr);
                }
            }

            Element deliveryParty = getFirstChildElementByLocalName(deliveryElem, "DeliveryParty");
            if (deliveryParty != null) {
                Element pName = getFirstChildElementByLocalName(deliveryParty, "PartyName");
                if (pName != null) {
                    dto.setCarrier(getFirstChildTextByLocalName(pName, "Name"));
                }
            }
        }

        // 10. Delivery Terms & Incoterms
        Element deliveryTermsElem = getFirstChildElementByLocalName(root, "DeliveryTerms");
        if (deliveryTermsElem != null) {
            String incotermId = getFirstChildTextByLocalName(deliveryTermsElem, "ID");
            dto.setIncoterms(mapIncoterms(incotermId));
            dto.setPaymentTerms(getFirstChildTextByLocalName(deliveryTermsElem, "SpecialTerms"));

            Element dLoc = getFirstChildElementByLocalName(deliveryTermsElem, "DeliveryLocation");
            if (dLoc != null) {
                dto.setIncotermsLocation(getFirstChildTextByLocalName(dLoc, "ID"));
            }
        }

        // 11. Allowances / Charges (Discounts & Freight)
        List<Element> allowanceCharges = getChildrenByLocalName(root, "AllowanceCharge");
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal freightTotal = BigDecimal.ZERO;

        for (Element ac : allowanceCharges) {
            String chargeIndicator = getFirstChildTextByLocalName(ac, "ChargeIndicator");
            String amountStr = getFirstChildTextByLocalName(ac, "Amount");
            BigDecimal amount = parseBigDecimalSafely(amountStr, BigDecimal.ZERO);

            if ("true".equalsIgnoreCase(chargeIndicator)) {
                freightTotal = freightTotal.add(amount);
            } else if ("false".equalsIgnoreCase(chargeIndicator)) {
                discountTotal = discountTotal.add(amount);
            }
        }

        dto.setDiscountAmount(discountTotal);
        dto.setFreightAmount(freightTotal);

        // 12. Tax Total
        Element taxTotalElem = getFirstChildElementByLocalName(root, "TaxTotal");
        if (taxTotalElem != null) {
            String taxAmountStr = getFirstChildTextByLocalName(taxTotalElem, "TaxAmount");
            dto.setTaxAmount(parseBigDecimalSafely(taxAmountStr, BigDecimal.ZERO));
        } else {
            dto.setTaxAmount(BigDecimal.ZERO);
        }

        // 13. Anticipated Monetary Totals
        Element totalElem = getFirstChildElementByLocalName(root, "AnticipatedMonetaryTotal");
        if (totalElem != null) {
            String lineExt = getFirstChildTextByLocalName(totalElem, "LineExtensionAmount");
            String payable = getFirstChildTextByLocalName(totalElem, "PayableAmount");
            dto.setSubtotalAmount(parseBigDecimalSafely(lineExt, BigDecimal.ZERO));
            dto.setTotalAmount(parseBigDecimalSafely(payable, BigDecimal.ZERO));
        }

        // Defaults for required header fields
        dto.setPoStatus(PoStatus.DRAFT);
        dto.setPriority(Priority.NORMAL);
        if (dto.getPaymentTerms() == null || dto.getPaymentTerms().isBlank()) {
            dto.setPaymentTerms("NET_30");
        }
        if (dto.getNetPaymentDays() == null) {
            dto.setNetPaymentDays(30);
        }

        // 14. Parse Line Items
        List<PurchaseOrderLineItemDto> lineItemDtos = mapLineItems(root, dto.getCurrencyCode());
        dto.setLineItems(lineItemDtos);

        // Recalculate or ensure Subtotal & Total
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (PurchaseOrderLineItemDto line : lineItemDtos) {
            if (line.getTotalMonetaryValue() != null) {
                calculatedSubtotal = calculatedSubtotal.add(line.getTotalMonetaryValue());
            }
        }

        if (dto.getSubtotalAmount() == null || dto.getSubtotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            dto.setSubtotalAmount(calculatedSubtotal);
        }

        if (dto.getTotalAmount() == null || dto.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal disc = dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal frt = dto.getFreightAmount() != null ? dto.getFreightAmount() : BigDecimal.ZERO;
            BigDecimal tax = dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO;
            dto.setTotalAmount(calculatedSubtotal.subtract(disc).add(frt).add(tax));
        }

        return dto;
    }

    private List<PurchaseOrderLineItemDto> mapLineItems(Element root, String headerCurrency) {
        List<PurchaseOrderLineItemDto> items = new ArrayList<>();
        List<Element> orderLines = getChildrenByLocalName(root, "OrderLine");

        int autoLineNumber = 1;

        for (Element orderLine : orderLines) {
            Element lineItemElem = getFirstChildElementByLocalName(orderLine, "LineItem");
            if (lineItemElem == null) {
                continue;
            }

            PurchaseOrderLineItemDto lineDto = new PurchaseOrderLineItemDto();

            // Line Number
            String lineIdStr = getFirstChildTextByLocalName(lineItemElem, "ID");
            int lineNum = autoLineNumber;
            if (lineIdStr != null && !lineIdStr.isBlank()) {
                try {
                    lineNum = Integer.parseInt(lineIdStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }
            lineDto.setLineNumber(lineNum);
            autoLineNumber = Math.max(autoLineNumber + 1, lineNum + 1);

            // Quantity & UOM
            Element qtyElem = getFirstChildElementByLocalName(lineItemElem, "Quantity");
            if (qtyElem != null) {
                String qtyStr = qtyElem.getTextContent();
                BigDecimal qty = parseBigDecimalSafely(qtyStr, BigDecimal.ONE);
                lineDto.setQuantityOrdered(qty);
                lineDto.setOriginalOrderQuantity(qty);

                String unitCode = qtyElem.getAttribute("unitCode");
                lineDto.setQuantityUom(mapUomCode(unitCode));
            } else {
                lineDto.setQuantityOrdered(BigDecimal.ONE);
                lineDto.setOriginalOrderQuantity(BigDecimal.ONE);
                lineDto.setQuantityUom("EA");
            }

            // Price
            Element priceElem = getFirstChildElementByLocalName(lineItemElem, "Price");
            if (priceElem != null) {
                Element priceAmountElem = getFirstChildElementByLocalName(priceElem, "PriceAmount");
                if (priceAmountElem != null) {
                    BigDecimal price = parseBigDecimalSafely(priceAmountElem.getTextContent(), BigDecimal.ZERO);
                    lineDto.setUnitPrice(price);

                    String lineCurrency = priceAmountElem.getAttribute("currencyID");
                    lineDto.setCurrencyCode(lineCurrency != null && !lineCurrency.isBlank() ? lineCurrency.toUpperCase() : headerCurrency);
                }
            }
            if (lineDto.getUnitPrice() == null) {
                lineDto.setUnitPrice(BigDecimal.ZERO);
            }
            if (lineDto.getCurrencyCode() == null) {
                lineDto.setCurrencyCode(headerCurrency);
            }

            // Line Extension Amount / Total Value
            String lineExtensionAmountStr = getFirstChildTextByLocalName(lineItemElem, "LineExtensionAmount");
            if (lineExtensionAmountStr != null && !lineExtensionAmountStr.isBlank()) {
                lineDto.setTotalMonetaryValue(parseBigDecimalSafely(lineExtensionAmountStr, lineDto.getQuantityOrdered().multiply(lineDto.getUnitPrice())));
            } else {
                lineDto.setTotalMonetaryValue(lineDto.getQuantityOrdered().multiply(lineDto.getUnitPrice()).setScale(4, RoundingMode.HALF_UP));
            }

            // Item Details
            Element itemElem = getFirstChildElementByLocalName(lineItemElem, "Item");
            if (itemElem != null) {
                String name = getFirstChildTextByLocalName(itemElem, "Name");
                String desc = getFirstChildTextByLocalName(itemElem, "Description");
                lineDto.setItemName(name != null && !name.isBlank() ? name : (desc != null ? desc : "Item " + lineNum));

                Element sellersItem = getFirstChildElementByLocalName(itemElem, "SellersItemIdentification");
                if (sellersItem != null) {
                    String sellerId = getFirstChildTextByLocalName(sellersItem, "ID");
                    lineDto.setSupplierItemCode(sellerId);
                    lineDto.setItemCode(sellerId);
                }

                Element standardItem = getFirstChildElementByLocalName(itemElem, "StandardItemIdentification");
                if (standardItem != null) {
                    String gtin = getFirstChildTextByLocalName(standardItem, "ID");
                    lineDto.setItemGtin(gtin);
                    if (lineDto.getItemCode() == null || lineDto.getItemCode().isBlank()) {
                        lineDto.setItemCode(gtin);
                    }
                }

                // Additional Item Properties (Sizes, Dimensions)
                List<Element> props = getChildrenByLocalName(itemElem, "AdditionalItemProperty");
                for (Element prop : props) {
                    String propName = getFirstChildTextByLocalName(prop, "Name");
                    String propVal = getFirstChildTextByLocalName(prop, "Value");
                    if (propName != null && propVal != null) {
                        if (lineDto.getPlannedSize1() == null) {
                            lineDto.setPlannedSize1(propVal);
                        } else if (lineDto.getPlannedSize2() == null) {
                            lineDto.setPlannedSize2(propVal);
                        }
                    }
                }
            }

            if (lineDto.getItemCode() == null || lineDto.getItemCode().isBlank()) {
                lineDto.setItemCode("ITEM-" + lineNum);
            }
            if (lineDto.getItemName() == null || lineDto.getItemName().isBlank()) {
                lineDto.setItemName(lineDto.getItemCode());
            }

            // Default enums and fields
            lineDto.setLineType(LineType.MATERIAL);
            lineDto.setProductClass(ProductClass.FINISHED_GOOD);
            lineDto.setLineStatus(LineStatus.OPEN);
            lineDto.setProtectionLevelCode(ProtectionLevelCode.NONE);
            lineDto.setQuantityReceived(BigDecimal.ZERO);
            lineDto.setShippedQuantity(BigDecimal.ZERO);

            items.add(lineDto);
        }

        return items;
    }

    // -------------------------------------------------------------------------
    // Helper Methods & Enum Mappings
    // -------------------------------------------------------------------------

    private PoType mapPoType(String contractType) {
        if (contractType == null || contractType.isBlank()) {
            return PoType.STANDARD;
        }
        String upper = contractType.toUpperCase();
        if (upper.contains("CONTRACT") || upper.contains("FRAMEWORK") || upper.contains("AGREEMENT")) {
            return PoType.CONTRACT;
        }
        if (upper.contains("BLANKET")) {
            return PoType.BLANKET;
        }
        if (upper.contains("SERVICE")) {
            return PoType.SERVICE;
        }
        if (upper.contains("CAPITAL")) {
            return PoType.CAPITAL;
        }
        if (upper.contains("SUBCONTRACT")) {
            return PoType.SUBCONTRACTING;
        }
        if (upper.contains("RETURN")) {
            return PoType.RETURN;
        }
        if (upper.contains("DROP")) {
            return PoType.DROP_SHIP;
        }
        if (upper.contains("EMERGENCY")) {
            return PoType.EMERGENCY;
        }
        return PoType.STANDARD;
    }

    private Incoterms mapIncoterms(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String upper = code.trim().toUpperCase();
        try {
            return Incoterms.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // Map legacy/transport specific terms
            if (upper.contains("FOT") || upper.contains("FOR")) {
                return Incoterms.FCA;
            }
            return Incoterms.FOB;
        }
    }

    private String mapUomCode(String unitCode) {
        if (unitCode == null || unitCode.isBlank()) {
            return "EA";
        }
        String code = unitCode.trim().toUpperCase();
        return switch (code) {
            case "C62", "EA", "PCE", "UNIT", "H87" -> "EA";
            case "PC", "PCS" -> "PC";
            case "BX", "BOX" -> "BOX";
            case "PF", "PALLET" -> "PALLET";
            case "KGM", "KG" -> "KG";
            case "LBR", "LBS", "LB" -> "LBS";
            case "MTR", "M" -> "M";
            case "FOT", "FT" -> "FT";
            case "LTR", "L" -> "L";
            case "GLL", "GAL" -> "GAL";
            case "SET" -> "SET";
            default -> "EA";
        };
    }

    private LocalDate parseDateSafely(String text, LocalDate fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(text.trim(), ISO_DATE_FORMATTER);
        } catch (Exception e) {
            return fallback;
        }
    }

    private BigDecimal parseBigDecimalSafely(String text, BigDecimal fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String buildLocationString(String street, String bldNo, String city) {
        List<String> parts = new ArrayList<>();
        if (street != null && !street.isBlank()) parts.add(street.trim());
        if (bldNo != null && !bldNo.isBlank()) parts.add(bldNo.trim());
        if (city != null && !city.isBlank()) parts.add(city.trim());
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    // -------------------------------------------------------------------------
    // DOM Navigation by Local Name
    // -------------------------------------------------------------------------

    private Element getFirstChildElementByLocalName(Element parent, String localName) {
        if (parent == null) return null;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeLocalName = node.getLocalName() != null ? node.getLocalName() : getUnprefixedName(node.getNodeName());
                if (localName.equalsIgnoreCase(nodeLocalName)) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    private List<Element> getChildrenByLocalName(Element parent, String localName) {
        List<Element> result = new ArrayList<>();
        if (parent == null) return result;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeLocalName = node.getLocalName() != null ? node.getLocalName() : getUnprefixedName(node.getNodeName());
                if (localName.equalsIgnoreCase(nodeLocalName)) {
                    result.add((Element) node);
                }
            }
        }
        return result;
    }

    private String getFirstChildTextByLocalName(Element parent, String localName) {
        Element elem = getFirstChildElementByLocalName(parent, localName);
        return elem != null ? elem.getTextContent().trim() : null;
    }

    private String getUnprefixedName(String qualifiedName) {
        if (qualifiedName == null) return null;
        int colonIdx = qualifiedName.indexOf(':');
        return colonIdx >= 0 ? qualifiedName.substring(colonIdx + 1) : qualifiedName;
    }
}
