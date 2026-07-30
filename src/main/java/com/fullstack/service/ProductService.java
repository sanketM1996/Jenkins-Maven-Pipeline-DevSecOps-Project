package com.fullstack.service;
// ProductService.java

import com.fullstack.exception.ResourceNotFoundException;
import com.fullstack.model.Product;
import com.fullstack.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // Pagination Logic
    public Page<Product> getProducts(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product createProduct(Product product) {
        return repository.save(product);
    }
}

