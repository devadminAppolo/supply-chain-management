package com.supplychain.wms.location.infrastructure.repository;

import com.supplychain.wms.location.domain.model.Aisle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AisleRepository extends JpaRepository<Aisle, Long>, JpaSpecificationExecutor<Aisle> {

    boolean existsByAisleCodeAndZoneIdAndDeletedFalse(String aisleCode, Long zoneId);

    boolean existsByAisleCodeAndZoneIdAndIdNotAndDeletedFalse(String aisleCode, Long zoneId, Long id);

    List<Aisle> findAllByZoneIdAndDeletedFalseOrderByAisleNameAsc(Long zoneId);

    Optional<Aisle> findByAisleNameAndZoneIdAndDeletedFalse(String aisleName, Long zoneId);

    Optional<Aisle> findFirstByAisleNameAndDeletedFalse(String aisleName);
}
