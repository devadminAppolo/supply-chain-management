package com.supplychain.service;

import com.supplychain.dto.DriverDto;

import java.util.List;

public interface DriverService {
    void save(DriverDto dto);

    DriverDto update(Long id, DriverDto dto);

    List<DriverDto> findAll();

}
