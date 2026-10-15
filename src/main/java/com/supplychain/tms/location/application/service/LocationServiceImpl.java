package com.supplychain.tms.location.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.LocationType;
import com.supplychain.tms.location.application.dto.LocationDto;
import com.supplychain.tms.location.domain.model.Location;
import com.supplychain.tms.location.infrastructure.mapper.LocationMapper;
import com.supplychain.tms.location.infrastructure.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository repository;
    private final LocationMapper     mapper;

    @Override
    @Transactional
    public LocationDto save(LocationDto dto) {
        if (repository.existsByLocationCodeAndDeletedFalse(dto.getLocationCode())) {
            log.warn("Duplicate location code: {}", dto.getLocationCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Location code already exists: " + dto.getLocationCode());
        }
        Location entity = mapper.toEntity(dto);
        entity.setDeleted(false);
        entity.setActive(true);
        Location saved = repository.save(entity);
        log.info("Created location id={}, code={}", saved.getId(), saved.getLocationCode());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<LocationDto> saveAll(List<LocationDto> dtos) {
        Set<String> seen = new HashSet<>();
        for (LocationDto dto : dtos) {
            if (!seen.add(dto.getLocationCode())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Duplicate location code in request: " + dto.getLocationCode());
            }
        }

        for (LocationDto dto : dtos) {
            if (repository.existsByLocationCodeAndDeletedFalse(dto.getLocationCode())) {
                log.warn("Duplicate location code on bulk create: {}", dto.getLocationCode());
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Location code already exists: " + dto.getLocationCode());
            }
        }

        List<Location> entities = dtos.stream()
                .map(dto -> {
                    Location entity = mapper.toEntity(dto);
                    entity.setDeleted(false);
                    entity.setActive(true);
                    return entity;
                })
                .toList();

        List<Location> saved = repository.saveAll(entities);
        log.info("Bulk-created {} locations", saved.size());
        return mapper.toDtoList(saved);
    }

    @Override
    @Transactional
    public LocationDto update(Long id, LocationDto dto) {
        Location entity = findActiveOrThrow(id);
        if (repository.existsByLocationCodeAndIdNotAndDeletedFalse(dto.getLocationCode(), id)) {
            log.warn("Duplicate location code {} on update", dto.getLocationCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Location code already exists: " + dto.getLocationCode());
        }
        mapper.updateEntity(dto, entity);
        Location saved = repository.save(entity);
        log.info("Updated location id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public LocationDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    public PageResponse<LocationDto> findAll(String search, LocationType locationType, Pageable pageable) {
        Specification<Location> spec = buildSpec(search, locationType);
        return PageResponse.of(repository.findAll(spec, pageable).map(mapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByLocationNameAsc()
                .stream()
                .map(l -> new DropdownOptionDto(
                        (l.getLocationCode() != null ? l.getLocationCode() + " - " : "") + l.getLocationName(),
                        String.valueOf(l.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Location entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted location id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Location> entities = repository.findAllById(ids).stream()
                .filter(l -> !l.isDeleted())
                .toList();
        entities.forEach(l -> l.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} locations", entities.size());
    }

    private Location findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(l -> !l.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Location not found with id: " + id));
    }

    private Specification<Location> buildSpec(String search, LocationType locationType) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);

            if (locationType != null) {
                pred = cb.and(pred, cb.equal(root.get("locationType"), locationType));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("locationCode")), pattern),
                        cb.like(cb.lower(root.get("locationName")), pattern),
                        cb.like(cb.lower(root.get("city")),         pattern),
                        cb.like(cb.lower(root.get("country")),      pattern)
                ));
            }
            return pred;
        };
    }
}
