package com.supplychain.service;

import com.supplychain.dto.ShipmentDto;

import java.util.List;

public interface ShipmentService {

    void save(ShipmentDto dto);

    ShipmentDto update(Long id, ShipmentDto dto);

    List<ShipmentDto> findAll();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
