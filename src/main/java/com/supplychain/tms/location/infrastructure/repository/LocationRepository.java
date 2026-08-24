package com.supplychain.tms.location.infrastructure.repository;

import com.supplychain.tms.location.domain.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {

    boolean existsByLocationCodeAndDeletedFalse(String locationCode);

    boolean existsByLocationCodeAndIdNotAndDeletedFalse(String locationCode, Long id);
}
