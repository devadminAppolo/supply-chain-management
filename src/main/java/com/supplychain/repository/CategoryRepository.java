package com.supplychain.repository;

import com.supplychain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    boolean existsByCategoryCodeAndDeletedFalse(String categoryCode);

    boolean existsByCategoryCodeAndIdNotAndDeletedFalse(String categoryCode, Long id);

    List<Category> findAllByDeletedFalseOrderByCategoryNameAsc();

    java.util.Optional<Category> findByCategoryNameAndDeletedFalse(String categoryName);
}
