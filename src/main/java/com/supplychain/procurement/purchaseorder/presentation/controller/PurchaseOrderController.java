package com.supplychain.procurement.purchaseorder.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderLineDto;
import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderDto;
import com.supplychain.procurement.purchaseorder.application.dto.XmlImportRequestDto;
import com.supplychain.procurement.purchaseorder.application.service.PurchaseOrderService;
import com.supplychain.procurement.purchaseorder.application.service.PurchaseOrderXmlParserService;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService          purchaseOrderService;
    private final PurchaseOrderXmlParserService xmlParserService;

    @PostMapping("/parse-xml")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> parseXmlJson(@RequestBody XmlImportRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(xmlParserService.parseXml(request.getXml())));
    }

    @PostMapping(value = "/parse-xml/raw", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> parseXmlRaw(@RequestBody String rawXml) {
        return ResponseEntity.ok(ApiResponse.success(xmlParserService.parseXml(rawXml)));
    }

    @PostMapping(value = "/parse-xml/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> parseXmlFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(xmlParserService.parseXml(file.getInputStream())));
    }

    @PostMapping("/import-xml")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> importXmlJson(@RequestBody XmlImportRequestDto request) {
        PurchaseOrderDto dto = xmlParserService.parseXml(request.getXml());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(purchaseOrderService.save(dto)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> save(@Valid @RequestBody PurchaseOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(purchaseOrderService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> update(@PathVariable Long id,
                                                                @Valid @RequestBody PurchaseOrderDto dto) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.update(id, dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PoStatus poStatus,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "poDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                purchaseOrderService.findAll(search, poStatus, supplier, warehouse, startDate, endDate, pageable)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown() {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.findDropdown()));
    }

    @GetMapping("/available-for-asn")
    public ResponseEntity<ApiResponse<PageResponse<AvailablePurchaseOrderDto>>> getAvailableForAsn(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "poDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.getAvailableForAsn(search, pageable)));
    }

    @GetMapping("/{poId}/available-asn-lines")
    public ResponseEntity<ApiResponse<List<AvailablePurchaseOrderLineDto>>> getAvailableAsnLines(@PathVariable Long poId) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.getAvailableAsnLines(poId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        purchaseOrderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        purchaseOrderService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
