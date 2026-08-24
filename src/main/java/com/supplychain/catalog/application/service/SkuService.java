package com.supplychain.catalog.application.service;

import com.supplychain.catalog.application.dto.SkuDto;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkuService {

    SkuDto save(SkuDto dto);

    SkuDto update(Long id, SkuDto dto);

    SkuDto findById(Long id);

    PageResponse<SkuDto> findAll(String search, Long productId, Pageable pageable);

    /** Returns active SKUs. If productId is supplied, results are scoped to that product. */
    List<DropdownOptionDto> findDropdown(Long productId);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
