package com.supplychain.wms.pallet.presentation.controller;

import com.supplychain.common.application.dto.ApiResponse;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.pallet.application.dto.CreatePalletRequestDto;
import com.supplychain.wms.pallet.application.dto.PalletResponseDto;
import com.supplychain.wms.pallet.application.dto.PalletStatusUpdateRequestDto;
import com.supplychain.wms.pallet.application.dto.UpdatePalletRequestDto;
import com.supplychain.wms.pallet.application.service.PalletService;
import com.supplychain.wms.pallet.domain.enums.PalletMaterial;
import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.enums.PalletType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.List;

@RestController
@RequestMapping("/api/pallets")
@RequiredArgsConstructor
public class PalletController {

    private final PalletService palletService;

    @PostMapping
    public ResponseEntity<ApiResponse<PalletResponseDto>> createPallet(@Valid @RequestBody CreatePalletRequestDto request) {
        PalletResponseDto created = palletService.createPallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pallet created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PalletResponseDto>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PalletStatus status,
            @RequestParam(required = false) PalletType type,
            @RequestParam(required = false) PalletMaterial material,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long dcId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findAll(search, status, type, material, active, orgId, dcId, pageable)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<PalletResponseDto>>> findAvailablePallets(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long dcId) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findAvailablePallets(orgId, dcId)));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionDto>>> findDropdown(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long dcId) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findDropdown(orgId, dcId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findById(id)));
    }

    @GetMapping("/number/{palletNumber}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> findByPalletNumber(@PathVariable String palletNumber) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findByPalletNumber(palletNumber)));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> findByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findByBarcode(barcode)));
    }

    @GetMapping("/lpn/{lpnNumber}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> findByLpnNumber(@PathVariable String lpnNumber) {
        return ResponseEntity.ok(ApiResponse.success(palletService.findByLpnNumber(lpnNumber)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PalletResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePalletRequestDto request) {
        PalletResponseDto updated = palletService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Pallet updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PalletResponseDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PalletStatusUpdateRequestDto request) {
        PalletResponseDto updated = palletService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Pallet status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        palletService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Pallet retired successfully", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteAllByIds(@RequestBody List<Long> ids) {
        palletService.deleteAllByIds(ids);
        return ResponseEntity.ok(ApiResponse.success("Selected pallets retired successfully", null));
    }
}
