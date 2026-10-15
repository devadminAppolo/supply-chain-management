package com.supplychain.tms.driver.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.tms.driver.application.dto.DriverDto;
import com.supplychain.tms.driver.domain.model.Driver;
import com.supplychain.tms.driver.infrastructure.mapper.DriverMapper;
import com.supplychain.tms.driver.infrastructure.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository repository;
    private final DriverMapper mapper;

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
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedDateIsNullOrderByDriverNameAsc()
                .stream()
                .map(d -> new DropdownOptionDto(
                        (d.getDriverName() != null ? d.getDriverName() : "") +
                                (d.getDriverNumber() != null ? " (" + d.getDriverNumber() + ")" : ""),
                        String.valueOf(d.getId())))
                .toList();
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
