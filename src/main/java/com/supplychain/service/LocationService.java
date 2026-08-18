package com.supplychain.service;

import com.supplychain.dto.LocationDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.enums.LocationType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {

    LocationDto save(LocationDto dto);

    List<LocationDto> saveAll(List<LocationDto> dtos);

    LocationDto update(Long id, LocationDto dto);

    LocationDto findById(Long id);

    PageResponse<LocationDto> findAll(String search, LocationType locationType, Pageable pageable);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
