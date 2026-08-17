package com.supplychain.service;

import com.supplychain.dto.DriverDto;
import com.supplychain.entity.Driver;
import com.supplychain.mapper.DriverMapper;
import com.supplychain.repository.DriverRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DriverServiceImpl implements DriverService{

    private final DriverRepository repository;
    private final DriverMapper mapper;

    public DriverServiceImpl(DriverRepository repository, DriverMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(DriverDto dto) {

        Driver driver = mapper.toEntity(dto);

        repository.save(driver);
    }

    @Override
    public DriverDto update(Long id, DriverDto dto) {
        Driver entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found with id: " + id));
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public List<DriverDto> findAll() {

        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        repository.deleteAllByIdInBatch(ids);
    }
}
