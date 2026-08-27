package com.supplychain.procurement.asn.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.procurement.asn.application.dto.AsnCreateRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnResponseDto;
import com.supplychain.procurement.asn.application.dto.AsnUpdateRequestDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderLineDto;
import com.supplychain.procurement.asn.application.service.AdvanceShippingNoticeService;
import com.supplychain.procurement.asn.domain.enums.AsnStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asns")
@RequiredArgsConstructor
public class AdvanceShippingNoticeController {

    private final AdvanceShippingNoticeService asnService;

    @PostMapping
    public ResponseEntity<ApiResponse<AsnResponseDto>> create(@Valid @RequestBody AsnCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Advance Shipping Notice created successfully", asnService.createAsn(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AsnResponseDto>> update(@PathVariable Long id,
                                                              @Valid @RequestBody AsnUpdateRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Advance Shipping Notice updated successfully", asnService.updateAsn(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AsnResponseDto>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Advance Shipping Notice cancelled successfully", asnService.cancelAsn(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AsnResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(asnService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AsnResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AsnStatus status,
            @RequestParam(required = false) Long purchaseOrderId,
            @RequestParam(required = false) String supplierId,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "shipmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                asnService.findAll(search, status, purchaseOrderId, supplierId, warehouseId, startDate, endDate, pageable)));
    }

    @GetMapping("/available-purchase-orders")
    public ResponseEntity<ApiResponse<PageResponse<AvailablePurchaseOrderDto>>> getAvailablePurchaseOrders(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "poDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(asnService.getAvailablePurchaseOrders(search, pageable)));
    }

    @GetMapping("/available-purchase-orders/{poId}/lines")
    public ResponseEntity<ApiResponse<List<AvailablePurchaseOrderLineDto>>> getAvailablePoLines(@PathVariable Long poId) {
        return ResponseEntity.ok(ApiResponse.success(asnService.getAvailablePoLines(poId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        asnService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody List<Long> ids) {
        asnService.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }
}
