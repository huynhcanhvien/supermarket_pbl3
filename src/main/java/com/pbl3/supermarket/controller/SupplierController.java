package com.pbl3.supermarket.controller;

import com.pbl3.supermarket.dto.request.SupplierRequest;
import com.pbl3.supermarket.dto.response.ApiResponse;
import com.pbl3.supermarket.dto.response.SupplierResponse;
import com.pbl3.supermarket.entity.Supplier;
import com.pbl3.supermarket.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public ApiResponse<SupplierResponse> addSupplier(@RequestBody SupplierRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .message("Supplier added successfully")
                .result(supplierService.createSupplier(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        return ApiResponse.<List<SupplierResponse>>builder()
                .message("Supplier list successfully")
                .result(supplierService.getAllSupplier())
                .build();
    }

    @PatchMapping
    public ApiResponse<SupplierResponse> updateSupplier(@RequestParam("supplierId") String supplierId,@RequestBody SupplierRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .message("Supplier updated successfully")
                .result(supplierService.updateSupplier(supplierId, request))
                .build();
    }
}
