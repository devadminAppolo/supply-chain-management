package com.supplychain.customer.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.customer.application.dto.CustomerRequestDto;
import com.supplychain.customer.application.dto.CustomerResponseDto;
import com.supplychain.customer.application.dto.CustomerUpdateDto;
import com.supplychain.customer.domain.model.Customer;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerResponseDto create(CustomerRequestDto dto);

    CustomerResponseDto update(Long id, CustomerUpdateDto dto);

    CustomerResponseDto findById(Long id);

    PageResponse<CustomerResponseDto> findAll(String search, Pageable pageable);

    List<CustomerResponseDto> findActive();

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);

    /**
     * Returns the raw entity for internal use by other services (e.g. OutboundOrderService).
     * Throws 404 if not found or deleted.
     */
    Customer getActiveEntityById(Long id);
}
