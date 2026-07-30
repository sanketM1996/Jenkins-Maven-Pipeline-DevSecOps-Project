package com.fullstack;

// ProductControllerTest.java

import com.fullstack.exception.ResourceNotFoundException;
import com.fullstack.model.Product;
import com.fullstack.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import com.fullstack.controller.ProductController;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest (ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void testGetProducts_Pagination() throws Exception {
        Product p1 = new Product("Laptop", 1200.0);
        PageImpl<Product> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 10), 1);

        Mockito.when(productService.getProducts(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetProductById_NotFoundException() throws Exception {
        Mockito.when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testCreateProduct_ValidationError() throws Exception {
        // Price is negative, which violates @Positive validation
        String invalidProductJson = "{\"name\":\"Phone\", \"price\":-500.0}";

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.price").value("Price must be greater than zero"));
    }
}