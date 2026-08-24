package com.supplychain.catalog.infrastructure.repository;

import com.supplychain.catalog.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    boolean existsByCategoryCodeAndDeletedFalse(String categoryCode);

    boolean existsByCategoryCodeAndIdNotAndDeletedFalse(String categoryCode, Long id);

    List<Category> findAllByDeletedFalseOrderByCategoryNameAsc();

    Optional<Category> findByCategoryNameAndDeletedFalse(String categoryName);
}
