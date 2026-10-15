package com.supplychain.tms.driver.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.tms.driver.application.dto.DriverDto;

import java.util.List;

public interface DriverService {

    void save(DriverDto dto);

    DriverDto update(Long id, DriverDto dto);

    List<DriverDto> findAll();

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
