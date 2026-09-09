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
import com.supplychain.wms.pallet.infrastructure.mapper.PalletMapper;
import com.supplychain.wms.pallet.infrastructure.repository.PalletRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class PalletServiceImpl implements PalletService {

    private final PalletRepository palletRepository;
    private final PalletMapper palletMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong sequenceGenerator = new AtomicLong(System.currentTimeMillis() % 100000);

    @Override
    @Transactional
    public PalletResponseDto createPallet(CreatePalletRequestDto request) {
        log.info("Creating new Pallet master record: type={}, material={}", request.getPalletType(), request.getPalletMaterial());

        // 1. Validate Barcode Uniqueness if provided
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            String trimmedBarcode = request.getBarcode().trim();
            if (palletRepository.existsByBarcodeAndDeletedFalse(trimmedBarcode)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode already exists: " + trimmedBarcode);
            }
        }

        // 2. Resolve or Generate LPN
        String lpnNumber;
        if (request.getCustomLpnNumber() != null && !request.getCustomLpnNumber().isBlank()) {
            lpnNumber = request.getCustomLpnNumber().trim();
            if (palletRepository.existsByLpnNumberAndDeletedFalse(lpnNumber)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "LPN number already exists: " + lpnNumber);
            }
        } else {
            lpnNumber = generateLpnNumber();
        }

        // 3. Generate Pallet Number
        String palletNumber = generatePalletNumber();

        // 4. Map Entity and Set Master Defaults
        Pallet pallet = palletMapper.toEntity(request);
        pallet.setPalletNumber(palletNumber);
        pallet.setLpnNumber(lpnNumber);
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            pallet.setBarcode(request.getBarcode().trim());
        }
        pallet.setPalletStatus(PalletStatus.AVAILABLE);
        pallet.setStatus(PalletStatus.AVAILABLE.name());
        pallet.setActive(true);
        pallet.setDeleted(false);

        Pallet saved = palletRepository.save(pallet);
        log.info("Pallet {} created successfully with ID: {} and LPN: {}", saved.getPalletNumber(), saved.getId(), saved.getLpnNumber());
        return palletMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PalletResponseDto findById(Long id) {
        Pallet pallet = palletRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with ID: " + id));
        return palletMapper.toDto(pallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PalletResponseDto findByPalletNumber(String palletNumber) {
        Pallet pallet = palletRepository.findByPalletNumberAndDeletedFalse(palletNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with Pallet Number: " + palletNumber));
        return palletMapper.toDto(pallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PalletResponseDto findByLpnNumber(String lpnNumber) {
        Pallet pallet = palletRepository.findByLpnNumberAndDeletedFalse(lpnNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with LPN Number: " + lpnNumber));
        return palletMapper.toDto(pallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PalletResponseDto findByBarcode(String barcode) {
        Pallet pallet = palletRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with Barcode: " + barcode));
        return palletMapper.toDto(pallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PalletResponseDto> findAll(String search, PalletStatus status, PalletType type, PalletMaterial material, Boolean active, Long orgId, Long dcId, Pageable pageable) {
        Specification<Pallet> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("palletStatus"), status));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("palletType"), type));
            }
            if (material != null) {
                predicates.add(cb.equal(root.get("palletMaterial"), material));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (orgId != null) {
                predicates.add(cb.equal(root.get("orgId"), orgId));
            }
            if (dcId != null) {
                predicates.add(cb.equal(root.get("dcId"), dcId));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("palletNumber")), pattern),
                        cb.like(cb.lower(root.get("lpnNumber")), pattern),
                        cb.like(cb.lower(root.get("barcode")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("notes")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Pallet> page = palletRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(palletMapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PalletResponseDto> findAvailablePallets(Long orgId, Long dcId) {
        List<Pallet> availablePallets = palletRepository.findAllByPalletStatusAndActiveTrueAndDeletedFalse(PalletStatus.AVAILABLE);
        if (orgId != null || dcId != null) {
            availablePallets = availablePallets.stream()
                    .filter(p -> (orgId == null || orgId.equals(p.getOrgId())) && (dcId == null || dcId.equals(p.getDcId())))
                    .toList();
        }
        return palletMapper.toDtoList(availablePallets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownOptionDto> findDropdown(Long orgId, Long dcId) {
        List<Pallet> pallets = palletRepository.findAllByPalletStatusAndActiveTrueAndDeletedFalse(PalletStatus.AVAILABLE);
        return pallets.stream()
                .filter(p -> (orgId == null || orgId.equals(p.getOrgId())) && (dcId == null || dcId.equals(p.getDcId())))
                .map(p -> new DropdownOptionDto(
                        String.valueOf(p.getId()),
                        String.format("%s (%s - %s %s)", p.getPalletNumber(), p.getLpnNumber(), p.getPalletType(), p.getPalletMaterial())
                ))
                .toList();
    }

    @Override
    @Transactional
    public PalletResponseDto update(Long id, UpdatePalletRequestDto request) {
        log.info("Updating Pallet ID: {}", id);

        Pallet pallet = palletRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with ID: " + id));

        // Validate barcode uniqueness if changing
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            String trimmedBarcode = request.getBarcode().trim();
            if (palletRepository.existsByBarcodeAndIdNotAndDeletedFalse(trimmedBarcode, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode already in use by another pallet: " + trimmedBarcode);
            }
        }

        // Prevent deactivating if pallet is actively IN_USE
        if (Boolean.FALSE.equals(request.getActive()) && pallet.getPalletStatus() == PalletStatus.IN_USE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot deactivate Pallet while it is currently IN_USE");
        }

        palletMapper.updateEntity(request, pallet);
        if (request.getActive() != null) {
            pallet.setActive(request.getActive());
        }

        Pallet saved = palletRepository.save(pallet);
        log.info("Pallet {} updated successfully", saved.getPalletNumber());
        return palletMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PalletResponseDto updateStatus(Long id, PalletStatusUpdateRequestDto request) {
        log.info("Updating status for Pallet ID: {} to {}", id, request.getStatus());

        Pallet pallet = palletRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with ID: " + id));

        PalletStatus currentStatus = pallet.getPalletStatus();
        PalletStatus targetStatus = request.getStatus();

        if (currentStatus == targetStatus) {
            return palletMapper.toDto(pallet);
        }

        // Business status transition validations
        if (currentStatus == PalletStatus.RETIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change status of a RETIRED pallet. Pallet must be reactivated first.");
        }

        pallet.setPalletStatus(targetStatus);
        pallet.setStatus(targetStatus.name());
        if (targetStatus == PalletStatus.RETIRED) {
            pallet.setActive(false);
        }
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            pallet.setRemarks(request.getRemarks());
        }

        Pallet saved = palletRepository.save(pallet);
        log.info("Pallet {} status updated: {} -> {}", saved.getPalletNumber(), currentStatus, targetStatus);
        return palletMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting (soft-delete/retire) Pallet ID: {}", id);

        Pallet pallet = palletRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet not found with ID: " + id));

        if (pallet.getPalletStatus() == PalletStatus.IN_USE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete or retire Pallet while it is currently IN_USE in warehouse operations");
        }

        pallet.setDeleted(true);
        pallet.setActive(false);
        pallet.setPalletStatus(PalletStatus.RETIRED);
        pallet.setStatus(PalletStatus.RETIRED.name());
        palletRepository.save(pallet);
        log.info("Pallet {} soft-deleted successfully", pallet.getPalletNumber());
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    @Transactional
    public Pallet resolveOrCreatePallet(Long palletMasterId, String palletNumber, String lpnNumber, Long orgId, Long dcId) {
        if (palletMasterId != null) {
            Pallet pallet = palletRepository.findByIdAndDeletedFalse(palletMasterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pallet master not found with ID: " + palletMasterId));

            if (!pallet.isActive() || pallet.getPalletStatus() != PalletStatus.AVAILABLE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("Pallet %s is not available for assignment (Status: %s, Active: %s)",
                                pallet.getPalletNumber(), pallet.getPalletStatus(), pallet.isActive()));
            }
            pallet.setPalletStatus(PalletStatus.IN_USE);
            pallet.setStatus(PalletStatus.IN_USE.name());
            return palletRepository.save(pallet);
        }

        // If palletNumber provided, check if existing
        if (palletNumber != null && !palletNumber.isBlank()) {
            var existingOpt = palletRepository.findByPalletNumberAndDeletedFalse(palletNumber.trim());
            if (existingOpt.isPresent()) {
                Pallet existing = existingOpt.get();
                if (!existing.isActive() || existing.getPalletStatus() != PalletStatus.AVAILABLE) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            String.format("Pallet %s is not available for assignment (Status: %s)",
                                    existing.getPalletNumber(), existing.getPalletStatus()));
                }
                existing.setPalletStatus(PalletStatus.IN_USE);
                existing.setStatus(PalletStatus.IN_USE.name());
                return palletRepository.save(existing);
            }
        }

        // Auto-create Pallet Master record for new physical pallet
        String finalPalletNumber = (palletNumber != null && !palletNumber.isBlank()) ? palletNumber.trim() : generatePalletNumber();
        String finalLpnNumber = (lpnNumber != null && !lpnNumber.isBlank()) ? lpnNumber.trim() : generateLpnNumber();

        Pallet newPallet = Pallet.builder()
                .orgId(orgId)
                .dcId(dcId)
                .palletNumber(finalPalletNumber)
                .lpnNumber(finalLpnNumber)
                .palletType(PalletType.STANDARD)
                .palletMaterial(PalletMaterial.WOOD)
                .palletStatus(PalletStatus.IN_USE)
                .status(PalletStatus.IN_USE.name())
                .active(true)
                .deleted(false)
                .build();

        return palletRepository.save(newPallet);
    }

    @Override
    @Transactional
    public void markPalletInUse(Long palletId) {
        if (palletId == null) return;
        palletRepository.findByIdAndDeletedFalse(palletId).ifPresent(pallet -> {
            pallet.setPalletStatus(PalletStatus.IN_USE);
            pallet.setStatus(PalletStatus.IN_USE.name());
            palletRepository.save(pallet);
        });
    }

    @Override
    @Transactional
    public void markPalletAvailable(Long palletId) {
        if (palletId == null) return;
        palletRepository.findByIdAndDeletedFalse(palletId).ifPresent(pallet -> {
            pallet.setPalletStatus(PalletStatus.AVAILABLE);
            pallet.setStatus(PalletStatus.AVAILABLE.name());
            palletRepository.save(pallet);
        });
    }

    private synchronized String generatePalletNumber() {
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        String candidate;
        do {
            long seq = sequenceGenerator.incrementAndGet();
            candidate = String.format("PAL-%s-%06d", datePrefix, seq % 1000000);
        } while (palletRepository.existsByPalletNumberAndDeletedFalse(candidate));
        return candidate;
    }

    private synchronized String generateLpnNumber() {
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        String candidate;
        do {
            long seq = sequenceGenerator.incrementAndGet();
            candidate = String.format("LPN-%s-%06d", datePrefix, seq % 1000000);
        } while (palletRepository.existsByLpnNumberAndDeletedFalse(candidate));
        return candidate;
    }
}
