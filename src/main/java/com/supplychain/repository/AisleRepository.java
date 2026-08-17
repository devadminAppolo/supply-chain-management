package com.supplychain.repository;

import com.supplychain.entity.Aisle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AisleRepository extends JpaRepository<Aisle, Long>, JpaSpecificationExecutor<Aisle> {

    boolean existsByAisleCodeAndZoneIdAndDeletedFalse(String aisleCode, Long zoneId);

    boolean existsByAisleCodeAndZoneIdAndIdNotAndDeletedFalse(String aisleCode, Long zoneId, Long id);

    List<Aisle> findAllByZoneIdAndDeletedFalseOrderByAisleNameAsc(Long zoneId);

    java.util.Optional<Aisle> findByAisleNameAndZoneIdAndDeletedFalse(String aisleName, Long zoneId);

    java.util.Optional<Aisle> findFirstByAisleNameAndDeletedFalse(String aisleName);
}
