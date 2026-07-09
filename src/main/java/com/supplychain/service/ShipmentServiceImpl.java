package com.supplychain.service;

import com.supplychain.dto.ShipmentDto;
import com.supplychain.entity.Shipment;
import com.supplychain.mapper.ShipmentMapper;
import com.supplychain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    @Override
    public void save(ShipmentDto dto) {

        Shipment shipment = mapper.toEntity(dto);

        repository.save(shipment);
    }

    @Override
    public List<ShipmentDto> findAll() {

        return mapper.toDtoList(repository.findAll());
    }
}