package com.supplychain.wms.staff.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.staff.application.dto.StaffDto;
import com.supplychain.wms.staff.domain.model.Staff;
import com.supplychain.wms.staff.infrastructure.mapper.StaffMapper;
import com.supplychain.wms.staff.infrastructure.repository.StaffRepository;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository repository;
    private final StaffMapper mapper;

    @Override
    @Transactional
    public StaffDto save(StaffDto dto) {
        if (repository.existsByUserNameAndDeletedFalse(dto.getUserName().trim())) {
            log.warn("Staff username already exists: {}", dto.getUserName());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Staff user name already exists: " + dto.getUserName());
        }

        Staff entity = mapper.toEntity(dto);
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("ACTIVE");
        }
        entity.setDeleted(false);

        Staff saved = repository.save(entity);
        log.info("Created Staff ID: {}, userName: {}", saved.getId(), saved.getUserName());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public StaffDto update(Long id, StaffDto dto) {
        Staff entity = findActiveOrThrow(id);

        if (repository.existsByUserNameAndIdNotAndDeletedFalse(dto.getUserName().trim(), id)) {
            log.warn("Staff username already exists on update: {}", dto.getUserName());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Staff user name already exists: " + dto.getUserName());
        }

        mapper.updateEntity(dto, entity);
        Staff saved = repository.save(entity);
        log.info("Updated Staff ID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDto findById(Long id) {
        Staff entity = findActiveOrThrow(id);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffDto> findAll(String search, String status, Long orgId, Long dcId, Pageable pageable) {
        Specification<Staff> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
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
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("userName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("role")), pattern),
                        cb.like(cb.lower(root.get("department")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Staff> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(mapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownOptionDto> findDropdown(Long orgId, Long dcId) {
        Specification<Staff> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            predicates.add(cb.equal(cb.upper(root.get("status")), "ACTIVE"));

            if (orgId != null) {
                predicates.add(cb.equal(root.get("orgId"), orgId));
            }
            if (dcId != null) {
                predicates.add(cb.equal(root.get("dcId"), dcId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec)
                .stream()
                .map(staff -> new DropdownOptionDto(
                        staff.getDisplayName(),
                        String.valueOf(staff.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Staff entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted Staff ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Staff> entities = repository.findAllById(ids)
                .stream()
                .filter(s -> !s.isDeleted())
                .toList();
        entities.forEach(s -> s.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} staff members", entities.size());
    }

    private Staff findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found with ID: " + id));
    }
}
