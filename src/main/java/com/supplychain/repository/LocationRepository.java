package com.supplychain.repository;

import com.supplychain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {

    boolean existsByLocationCodeAndDeletedFalse(String locationCode);

    boolean existsByLocationCodeAndIdNotAndDeletedFalse(String locationCode, Long id);
}
