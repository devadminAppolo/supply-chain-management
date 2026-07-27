package com.supplychain.service;

import com.supplychain.dto.DriverDto;
import com.supplychain.dto.LocationDto;

import java.util.List;

public interface LocationService {

    void save(LocationDto dto);

    LocationDto update(Long id, LocationDto dto);

    List<LocationDto> findAll();

}

