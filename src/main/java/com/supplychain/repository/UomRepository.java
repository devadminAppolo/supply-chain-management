package com.supplychain.repository;

import com.supplychain.entity.Uom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UomRepository extends JpaRepository<Uom, Long>, JpaSpecificationExecutor<Uom> {

    boolean existsByUomCodeAndDeletedFalse(String uomCode);

    boolean existsByUomCodeAndIdNotAndDeletedFalse(String uomCode, Long id);

    List<Uom> findAllByDeletedFalseOrderByUomNameAsc();

    java.util.Optional<Uom> findByUomNameAndDeletedFalse(String uomName);
}
