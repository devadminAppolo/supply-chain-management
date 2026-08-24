package com.supplychain.tms.driver.application.service;

import com.supplychain.tms.driver.application.dto.DriverDto;

import java.util.List;

public interface DriverService {

    void save(DriverDto dto);

    DriverDto update(Long id, DriverDto dto);

    List<DriverDto> findAll();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
