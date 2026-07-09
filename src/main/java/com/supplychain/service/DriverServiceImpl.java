package com.supplychain.service;

import com.supplychain.dto.DriverDto;
import com.supplychain.entity.Driver;
import com.supplychain.mapper.DriverMapper;
import com.supplychain.repository.DriverRepository;
import org.springframework.stereotype.Service;

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
    public List<DriverDto> findAll() {

        return mapper.toDtoList(repository.findAll());
    }
}
