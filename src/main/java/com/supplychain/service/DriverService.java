package com.supplychain.service;

import com.supplychain.dto.DriverDto;

import java.util.List;

public interface DriverService {
    void save(DriverDto dto);

    List<DriverDto> findAll();

}
