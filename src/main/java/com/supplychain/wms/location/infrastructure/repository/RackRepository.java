package com.supplychain.wms.location.infrastructure.repository;

import com.supplychain.wms.location.domain.model.Rack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RackRepository extends JpaRepository<Rack, Long>, JpaSpecificationExecutor<Rack> {

    boolean existsByRackCodeAndAisleIdAndDeletedFalse(String rackCode, Long aisleId);

    boolean existsByRackCodeAndAisleIdAndIdNotAndDeletedFalse(String rackCode, Long aisleId, Long id);

    List<Rack> findAllByAisleIdAndDeletedFalseOrderByRackNameAsc(Long aisleId);

    Optional<Rack> findByRackNameAndAisleIdAndDeletedFalse(String rackName, Long aisleId);

    Optional<Rack> findFirstByRackNameAndDeletedFalse(String rackName);
}
