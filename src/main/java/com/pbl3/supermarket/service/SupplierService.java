package com.pbl3.supermarket.service;

import com.pbl3.supermarket.dto.request.SupplierRequest;
import com.pbl3.supermarket.dto.response.SupplierResponse;
import com.pbl3.supermarket.entity.Supplier;
import com.pbl3.supermarket.exception.AppException;
import com.pbl3.supermarket.exception.ErrorCode;
import com.pbl3.supermarket.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;
    private SupplierResponse toSupplierResponse(Supplier supplier){

        SupplierResponse supplierResponse = new SupplierResponse();
        supplierResponse.setId(supplier.getId());
        supplierResponse.setName(supplier.getName());
        supplierResponse.setAddress(supplier.getAddress());
        supplierResponse.setPhone(supplier.getPhone());
        supplierResponse.setEmail(supplier.getEmail());
        return supplierResponse;
    }
    public SupplierResponse createSupplier(SupplierRequest request)
    {
        if(supplierRepository.existsByName(request.getName()))
        {
            throw new AppException(ErrorCode.SUPPLIER_NAME_EXISTED);
        }
        else {

            Supplier supplier = new Supplier();
            supplier.setName(request.getName());
            supplier.setAddress(request.getAddress());
            supplier.setPhone(request.getPhone());
            supplier.setEmail(request.getEmail());

            return toSupplierResponse(supplierRepository.save(supplier));
        }
    }

    public SupplierResponse updateSupplier(String id, SupplierRequest request)
    {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_ID_NOTFOUND));

        if(request.getAddress() != null && !request.getAddress().isEmpty())
        {
            supplier.setAddress(request.getAddress());
        }
        if(request.getPhone() != null && !request.getPhone().isEmpty()){
            supplier.setPhone(request.getPhone());
        }
        if(request.getEmail() != null && !request.getEmail().isEmpty()){
            supplier.setEmail(request.getEmail());
        }

        return toSupplierResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> getAllSupplier(){
        List<SupplierResponse> supplierResponses = new ArrayList<>();
        for(Supplier supplier : supplierRepository.findAll()){
            supplierResponses.add(toSupplierResponse(supplier));
        }

        return supplierResponses;
    }
}
