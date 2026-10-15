package com.supplychain.customer.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.customer.application.dto.CustomerRequestDto;
import com.supplychain.customer.application.dto.CustomerResponseDto;
import com.supplychain.customer.application.dto.CustomerUpdateDto;
import com.supplychain.customer.domain.enums.CustomerStatus;
import com.supplychain.customer.domain.model.Customer;
import com.supplychain.customer.infrastructure.mapper.CustomerMapper;
import com.supplychain.customer.infrastructure.repository.CustomerRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper     mapper;

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Override
    @Transactional
    public CustomerResponseDto create(CustomerRequestDto dto) {
        String code = dto.getCustomerCode().trim();

        if (repository.existsByCustomerCodeAndDeletedFalse(code)) {
            log.warn("Duplicate customer code on create: {}", code);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Customer code already exists: " + code);
        }

        Customer entity = mapper.toEntity(dto);
        entity.setCustomerCode(code);
        entity.setStatus(dto.getStatus().name());
        entity.setDeleted(false);

        Customer saved = repository.save(entity);
        log.info("Created customer id={}, code={}", saved.getId(), saved.getCustomerCode());
        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    @Override
    @Transactional
    public CustomerResponseDto update(Long id, CustomerUpdateDto dto) {
        Customer entity = findActiveOrThrow(id);
        String code = dto.getCustomerCode().trim();

        if (repository.existsByCustomerCodeAndIdNotAndDeletedFalse(code, id)) {
            log.warn("Duplicate customer code {} on update for id={}", code, id);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Customer code already exists: " + code);
        }

        mapper.updateEntity(dto, entity);
        entity.setCustomerCode(code);
        entity.setStatus(dto.getStatus().name());

        Customer saved = repository.save(entity);
        log.info("Updated customer id={}", saved.getId());
        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponseDto> findAll(String search, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search), pageable).map(mapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDto> findActive() {
        return mapper.toDtoList(
                repository.findAllByStatusAndDeletedFalse(CustomerStatus.ACTIVE.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByCustomerNameAsc()
                .stream()
                .map(c -> new DropdownOptionDto(c.getCustomerName(), String.valueOf(c.getId())))
                .toList();
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    @Override
    @Transactional
    public void deleteById(Long id) {
        Customer entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted customer id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Customer> entities = repository.findAllById(ids).stream()
                .filter(c -> !c.isDeleted())
                .toList();
        entities.forEach(c -> c.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} customers", entities.size());
    }

    // -------------------------------------------------------
    // Internal: entity access for other services
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Customer getActiveEntityById(Long id) {
        return findActiveOrThrow(id);
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private Customer findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Customer not found with id: " + id));
    }

    private Specification<Customer> buildSpec(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerCode")), pattern),
                        cb.like(cb.lower(root.get("customerName")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
