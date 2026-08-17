package com.supplychain.repository;

import com.supplychain.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor<Zone> {

    boolean existsByZoneCodeAndWarehouseIdAndDeletedFalse(String zoneCode, Long warehouseId);

    boolean existsByZoneCodeAndWarehouseIdAndIdNotAndDeletedFalse(String zoneCode, Long warehouseId, Long id);

    List<Zone> findAllByWarehouseIdAndDeletedFalseOrderByZoneNameAsc(Long warehouseId);

    java.util.Optional<Zone> findByZoneNameAndWarehouseIdAndDeletedFalse(String zoneName, Long warehouseId);

    java.util.Optional<Zone> findFirstByZoneNameAndDeletedFalse(String zoneName);
}
