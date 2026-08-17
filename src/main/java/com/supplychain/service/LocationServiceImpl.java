package com.supplychain.service;

import com.supplychain.dto.LocationDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.entity.Location;
import com.supplychain.enums.LocationType;
import com.supplychain.mapper.LocationMapper;
import com.supplychain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository repository;
    private final LocationMapper     mapper;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    public LocationDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    public PageResponse<LocationDto> findAll(String search, LocationType locationType, Pageable pageable) {
        Specification<Location> spec = buildSpec(search, locationType);
        return PageResponse.of(repository.findAll(spec, pageable).map(mapper::toDto));
    }

    // -------------------------------------------------------------------------
    // Delete (soft)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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
