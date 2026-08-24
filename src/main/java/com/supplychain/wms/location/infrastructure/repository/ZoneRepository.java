package com.supplychain.wms.location.infrastructure.repository;

import com.supplychain.wms.location.domain.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor<Zone> {

    boolean existsByZoneCodeAndWarehouseIdAndDeletedFalse(String zoneCode, Long warehouseId);

    boolean existsByZoneCodeAndWarehouseIdAndIdNotAndDeletedFalse(String zoneCode, Long warehouseId, Long id);

    List<Zone> findAllByWarehouseIdAndDeletedFalseOrderByZoneNameAsc(Long warehouseId);

    Optional<Zone> findByZoneNameAndWarehouseIdAndDeletedFalse(String zoneName, Long warehouseId);

    Optional<Zone> findFirstByZoneNameAndDeletedFalse(String zoneName);
}
