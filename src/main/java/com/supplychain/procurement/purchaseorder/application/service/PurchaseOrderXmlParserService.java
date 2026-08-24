package com.supplychain.procurement.purchaseorder.application.service;

import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderDto;

import java.io.InputStream;

public interface PurchaseOrderXmlParserService {

    /**
     * Parses an XML string (supporting UBL 2.0/2.1 Order format or standard PO XML)
     * and maps it to a PurchaseOrderDto including line items.
     *
     * @param xmlContent XML string
     * @return Populated PurchaseOrderDto
     */
    PurchaseOrderDto parseXml(String xmlContent);

    /**
     * Parses an XML input stream and maps it to a PurchaseOrderDto.
     *
     * @param inputStream XML input stream
     * @return Populated PurchaseOrderDto
     */
    PurchaseOrderDto parseXml(InputStream inputStream);
}
