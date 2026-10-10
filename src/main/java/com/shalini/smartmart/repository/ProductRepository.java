package com.shalini.smartmart.repository;

import com.shalini.smartmart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId);
    List<Product> findByStockLessThanEqual(Integer stock);
    long countByCategoryId(Long categoryId);
}
