package com.supplychain.tms.location.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.LocationType;
import com.supplychain.tms.location.application.dto.LocationDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {

    LocationDto save(LocationDto dto);

    List<LocationDto> saveAll(List<LocationDto> dtos);

    LocationDto update(Long id, LocationDto dto);

    LocationDto findById(Long id);

    PageResponse<LocationDto> findAll(String search, LocationType locationType, Pageable pageable);

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
