package com.supplychain.service;

import com.supplychain.dto.LocationDto;
import com.supplychain.entity.Location;
import com.supplychain.mapper.LocationMapper;
import com.supplychain.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService{

    private final LocationRepository repository;
    private final LocationMapper mapper;

    public LocationServiceImpl(LocationRepository repository, LocationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(LocationDto dto) {

        Location shipment = mapper.toEntity(dto);

        repository.save(shipment);
    }

    @Override
    public LocationDto update(Long id, LocationDto dto) {
        Location entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found with id: " + id));
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public List<LocationDto> findAll() {

        return mapper.toDtoList(repository.findAll());
    }
}
