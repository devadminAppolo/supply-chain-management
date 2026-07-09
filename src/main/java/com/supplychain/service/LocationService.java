package com.supplychain.service;

import com.supplychain.dto.DriverDto;
import com.supplychain.dto.LocationDto;

import java.util.List;

public interface LocationService {

    void save(LocationDto dto);

    List<LocationDto> findAll();

}

