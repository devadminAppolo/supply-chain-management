package com.supplychain.catalog.infrastructure.repository;

import com.supplychain.catalog.domain.model.Uom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UomRepository extends JpaRepository<Uom, Long>, JpaSpecificationExecutor<Uom> {

    boolean existsByUomCodeAndDeletedFalse(String uomCode);

    boolean existsByUomCodeAndIdNotAndDeletedFalse(String uomCode, Long id);

    List<Uom> findAllByDeletedFalseOrderByUomNameAsc();

    Optional<Uom> findByUomNameAndDeletedFalse(String uomName);
}
