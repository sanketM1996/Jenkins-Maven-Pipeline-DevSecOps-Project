package com.fullstack.repository;



import com.fullstack.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository inherently supports pagination via findAll(Pageable pageable)
}