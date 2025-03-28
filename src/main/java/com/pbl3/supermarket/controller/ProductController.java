package com.pbl3.supermarket.controller;

import com.pbl3.supermarket.dto.request.ProductCreationRequest;
import com.pbl3.supermarket.dto.request.SearchProductByCategoriesRequest;
import com.pbl3.supermarket.dto.response.ApiResponse;
import com.pbl3.supermarket.dto.response.ProductResponse;
import com.pbl3.supermarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .message("Product created successfully")
                .result(productService.createProduct(request))
                .build();
    }
    @GetMapping("/all")
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .message("All products")
                .result(productService.getAllProducts())
                .build();
    }
    @GetMapping("/{productID}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable("productID") String productId) {
        return ApiResponse.<ProductResponse>builder()
                .message("[OK] Get Product by Id")
                .result(productService.getProductById(productId))
                .build();
    }
    @DeleteMapping("/{productID}")
    public ApiResponse<Boolean> deleteProduct(@PathVariable("productID") String productId) {
        return ApiResponse.<Boolean>builder()
                .message("[OK] Already deleted productId: " + productId)
                .result(productService.deleteProductById(productId))
                .build();
    }
    @GetMapping("/search/{keySearch}")
    public ApiResponse<List<ProductResponse>> getProductsByKeySearch(@PathVariable("keySearch") String keySearch) {
        return ApiResponse.<List<ProductResponse>>builder()
                .message("[OK] Get Products by Key Search: " + keySearch)
                .result(productService.searchProducts(keySearch))
                .build();
    }
    @GetMapping("/searchByCategories")
    public ApiResponse<List<ProductResponse>> getProductsByCategories(@RequestBody SearchProductByCategoriesRequest request) {
        return ApiResponse.<List<ProductResponse>>builder()
                .message("[OK] Get Products by Categories")
                .result(productService.searchProductsByCategories(request))
                .build();
    }
}
