package com.supplychain.wms.pallet.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.pallet.application.dto.CreatePalletRequestDto;
import com.supplychain.wms.pallet.application.dto.PalletResponseDto;
import com.supplychain.wms.pallet.application.dto.PalletStatusUpdateRequestDto;
import com.supplychain.wms.pallet.application.dto.UpdatePalletRequestDto;
import com.supplychain.wms.pallet.domain.enums.PalletMaterial;
import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.enums.PalletType;
import com.supplychain.wms.pallet.domain.model.Pallet;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PalletService {

    PalletResponseDto createPallet(CreatePalletRequestDto request);

    PalletResponseDto findById(Long id);

    PalletResponseDto findByPalletNumber(String palletNumber);

    PalletResponseDto findByLpnNumber(String lpnNumber);

    PalletResponseDto findByBarcode(String barcode);

    PageResponse<PalletResponseDto> findAll(String search, PalletStatus status, PalletType type, PalletMaterial material, Boolean active, Long orgId, Long dcId, Pageable pageable);

    List<PalletResponseDto> findAvailablePallets(Long orgId, Long dcId);

    List<DropdownOptionDto> findDropdown(Long orgId, Long dcId);

    PalletResponseDto update(Long id, UpdatePalletRequestDto request);

    PalletResponseDto updateStatus(Long id, PalletStatusUpdateRequestDto request);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);

    Pallet resolveOrCreatePallet(Long palletMasterId, String palletNumber, String lpnNumber, Long orgId, Long dcId);

    void markPalletInUse(Long palletId);

    void markPalletAvailable(Long palletId);
}
