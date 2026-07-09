package com.supplychain.service;

import com.supplychain.dto.ShipmentDto;

import java.util.List;

public interface ShipmentService {

    void save(ShipmentDto dto);

    List<ShipmentDto> findAll();
}
