package com.supplychain.tms.shipment.application.service;

import com.supplychain.tms.shipment.application.dto.ShipmentDto;

import java.util.List;

public interface ShipmentService {

    void save(ShipmentDto dto);

    ShipmentDto update(Long id, ShipmentDto dto);

    List<ShipmentDto> findAll();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
