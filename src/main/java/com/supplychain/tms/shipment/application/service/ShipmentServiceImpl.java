package com.supplychain.tms.shipment.application.service;

import com.supplychain.tms.shipment.application.dto.ShipmentDto;
import com.supplychain.tms.shipment.domain.model.Shipment;
import com.supplychain.tms.shipment.infrastructure.mapper.ShipmentMapper;
import com.supplychain.tms.shipment.infrastructure.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public ShipmentDto update(Long id, ShipmentDto dto) {
        Shipment entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found with id: " + id));
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public List<ShipmentDto> findAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        repository.deleteAllByIdInBatch(ids);
    }
}
