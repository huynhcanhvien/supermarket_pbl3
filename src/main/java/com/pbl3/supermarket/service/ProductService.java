package com.pbl3.supermarket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl3.supermarket.dto.request.ProductCreationRequest;
import com.pbl3.supermarket.dto.request.ProductUpdateRequest;
import com.pbl3.supermarket.dto.request.SearchProductByCategoriesRequest;
import com.pbl3.supermarket.dto.response.CategoryResponse;
import com.pbl3.supermarket.dto.response.ProductResponse;
import com.pbl3.supermarket.dto.response.SupplierResponse;
import com.pbl3.supermarket.entity.Category;
import com.pbl3.supermarket.entity.Product;
import com.pbl3.supermarket.entity.Supplier;
import com.pbl3.supermarket.exception.AppException;
import com.pbl3.supermarket.exception.ErrorCode;
import com.pbl3.supermarket.mapper.ProductMapper;
import com.pbl3.supermarket.repository.CategoryRepository;
import com.pbl3.supermarket.repository.ProductRepository;
import com.pbl3.supermarket.repository.SupplierRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    SupplierRepository supplierRepository;
    private SupplierResponse toSupplierResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .email(supplier.getEmail())
                .name(supplier.getName())
                .build();
    }
    private List<CategoryResponse> toCategoryResponse(List<Category> categories) {
        List<CategoryResponse> categoriesResponse = new ArrayList<>();
        for(Category category : categories) {
            categoriesResponse.add(
                    CategoryResponse.builder()
                            .id(category.getId())
                            .description(category.getDescription())
                            .name(category.getName())
                            .build()
            );
        }
        return categoriesResponse;
    }
    private ProductResponse toProductResponse(Product product, SupplierResponse supplierResponse, List<CategoryResponse> categoriesResponse) {
        return ProductResponse.builder()
                .id(product.getId())
                .supplier(supplierResponse)
                .category(categoriesResponse.getFirst())
                .createDate(product.getCreateDate())
                .discount(product.getDiscount())
                .price(product.getPrice())
                .expiryDate(product.getExpiryDate())
                .unit_price(product.getUnit_price())
                .quantity(product.getStockQuantity())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .build();
    }

    public ProductResponse createProduct(ProductCreationRequest productCreationRequest) {
        if(productRepository.existsByName(productCreationRequest.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTED);
        }
        else {
            Product product = productMapper.toProduct(productCreationRequest);
            String supplierId = productCreationRequest.getSupplierId();
            Long[] categoryIds = productCreationRequest.getCategoryId();
            List<Category> categories = new ArrayList<>();
            Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_ID_NOTFOUND));
            for (Long categoryId : categoryIds) {
                Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_ID_NOTFOUND));
                categories.add(category);
            }
            product.setSupplier(supplier);
            product.setCategories(categories);
            product.setNBuy(0);
            SupplierResponse supplierResponse = toSupplierResponse(supplier);
            List<CategoryResponse> categoriesResponse = toCategoryResponse(categories);
            productRepository.save(product);
            return toProductResponse(product, supplierResponse, categoriesResponse);
        }
    }
    public ProductResponse updateProduct(String productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ID_NOTFOUND));

        if(request.getName() != null)
            product.setName(request.getName());
        if(request.getImageUrl() != null)
            product.setImageUrl(request.getImageUrl());

        if(request.getNewCategoryId() != null){
            List<Category> categories = new ArrayList<>();
            categories.add(categoryRepository.findById(request.getNewCategoryId()).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_ID_NOTFOUND)));
            product.setCategories(categories);
        }

        if(request.getNewSupplierId() != null){
            Supplier supplier = supplierRepository.findById(request.getNewSupplierId()).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_ID_NOTFOUND));
            product.setSupplier(supplier);
        }

        if(request.getPrice() != product.getPrice() && request.getPrice() != 0.0) {
            product.setPrice(request.getPrice());
        }
        if(request.getDiscount() != product.getDiscount()) {
            product.setDiscount(request.getDiscount());
        }

        if(request.getStockQuantity() != product.getStockQuantity()) {
            product.setStockQuantity(request.getStockQuantity());
        }

        if(request.getUnit_price() != null) {
            product.setUnit_price(request.getUnit_price());
        }

        if(request.getCreateDate() != null){
            product.setCreateDate(request.getCreateDate());
        }
        if(request.getExpiryDate() != null){
            product.setExpiryDate(request.getExpiryDate());
        }

        return productRepository.save(product).toProductResponse();
    }
    public List<ProductResponse> getAllProducts() {
        List<ProductResponse> productResponses = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            SupplierResponse supplierResponse = toSupplierResponse(product.getSupplier());

            List<CategoryResponse> categoriesResponse = toCategoryResponse(product.getCategories());
            productResponses.add(
                toProductResponse(product, supplierResponse, categoriesResponse)
            );
        }
        return productResponses;
    }

    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ID_NOTFOUND));
        SupplierResponse supplierResponse = toSupplierResponse(product.getSupplier());
        List<CategoryResponse> categoriesResponse = toCategoryResponse(product.getCategories());
        return toProductResponse(product, supplierResponse, categoriesResponse);
    }

    public boolean deleteProductById(String productId)
    {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ID_NOTFOUND));
        if(product.getStockQuantity() > 0)
            throw new AppException(ErrorCode.PRODUCT_STILL_AVAILABLE);
        productRepository.delete(product);
        return true;
    }

    public List<ProductResponse> searchProducts(String keyword)
    {
        String keywordLower = keyword.toLowerCase();
        List<ProductResponse> productResponses = new ArrayList<>();

        for(Product product : productRepository.findAll()) {
            if(product.getName().toLowerCase().contains(keywordLower)) {
                productResponses.add(toProductResponse(product, toSupplierResponse(product.getSupplier()), toCategoryResponse(product.getCategories())));

            }
        }
        return productResponses;
    }

    public List<ProductResponse> searchProductsByCategories(SearchProductByCategoriesRequest request)
    {   List<Long> categoriesIds = request.getCategoryIds();
        List<Product> products = productRepository.findByCategories(categoriesIds);
        List<ProductResponse> productResponses = new ArrayList<>();
        products.forEach(product -> {
            SupplierResponse supplierResponse = toSupplierResponse(product.getSupplier());
            List<CategoryResponse> categoriesResponse = toCategoryResponse(product.getCategories());
            productResponses.add(toProductResponse(product, supplierResponse, categoriesResponse));
        });
        return productResponses;
    }

    public Map<String, List<ProductResponse> > getALlProductByCategories()
    {
        Map<String, List<ProductResponse> > results = new HashMap<>();
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()){
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        for(Category category : categories) {
            List<Product> products = category.getProducts();
            List<ProductResponse> productResponses = new ArrayList<>();
            for(Product product : products) {
                productResponses.add(product.toProductResponse());
            }
            results.put(category.getName(), productResponses);
        }

        return results;
    }
}
