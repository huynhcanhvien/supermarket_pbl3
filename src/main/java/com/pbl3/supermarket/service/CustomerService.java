package com.pbl3.supermarket.service;

import com.pbl3.supermarket.dto.request.CustomerRequest.CustomerCreationRequest;
import com.pbl3.supermarket.dto.request.CustomerRequest.CustomerUpdateRequest;
import com.pbl3.supermarket.dto.response.CartItemResponse;
import com.pbl3.supermarket.dto.response.CustomerResponse;
import com.pbl3.supermarket.dto.response.ProductResponse;
import com.pbl3.supermarket.dto.response.ReceiptResponse;
import com.pbl3.supermarket.entity.*;
import com.pbl3.supermarket.enums.ROLES;
import com.pbl3.supermarket.exception.AppException;
import com.pbl3.supermarket.exception.ErrorCode;
import com.pbl3.supermarket.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
public class CustomerService {
    @Autowired
    CartRepository cartRepository;
    @Autowired
    CustomerRepository customerRepository;

    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;

    // Register:
    private Cart createCart(){
        Cart cart = new Cart();
        return cartRepository.save(cart);
    }
    public CustomerResponse createCustomer(CustomerCreationRequest request){
        //username must be unique
        String username = request.getUsername();
        if(customerRepository.existsByUsername(username)){
            throw new AppException(ErrorCode.USERNAME_IS_EXISTED);
        }
        //set property:
        Customer customer = new Customer();
        customer.setUsername(request.getUsername());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setEmail(request.getEmail());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setBirthDate(request.getBirthDate());
        customer.setPoint(0);

        Set<ROLES> roles = new HashSet<>();
        roles.add(ROLES.CUSTOMER);
        customer.setRoles(roles);
        customer.setCreatedDate(LocalDate.now());

        customer.setCart(createCart());
        customerRepository.save(customer);
        return customerToCustomerResponse(customer);
    }

    public CustomerResponse getCustomerById(String id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_ID_NOTFOUND));
        return customerToCustomerResponse(customer);
    }

    public List<CustomerResponse> getAllCustomer(){
        List<CustomerResponse> customerResponses = new ArrayList<>();
        for (Customer customer : customerRepository.findAll()) {
            customerResponses.add(customerToCustomerResponse(customer));
        }
        return customerResponses;
    }
    public CustomerResponse deleteCustomerById(String id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_ID_NOTFOUND));
        CustomerResponse customerResponse = customerToCustomerResponse(customer);
        customerRepository.delete(customer);
        return customerResponse;
    }

    public CustomerResponse updateCustomer(String id, CustomerUpdateRequest request){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_ID_NOTFOUND));
        if(request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if(request.getLastName() != null) customer.setLastName(request.getLastName());
        if(request.getPhone() != null) customer.setPhone(request.getPhone());
        if(request.getAddress() != null) customer.setAddress(request.getAddress());
        if(request.getBirthDate() != null) customer.setBirthDate(request.getBirthDate());
        if(request.getEmail() != null) customer.setEmail(request.getEmail());
        if(request.getPassword() != null && request.getCurrent_password() != null) {
            if(passwordEncoder.matches(request.getCurrent_password(), customer.getPassword())) {
                customer.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }
        customerRepository.save(customer);
        return customerToCustomerResponse(customer);
    }

    private CustomerResponse customerToCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .birthDate(customer.getBirthDate())
                .email(customer.getEmail())
                .points(customer.getPoint())
                .username(customer.getUsername())
                .createdDate(customer.getCreatedDate())
                .build();
    }

    public List<CartItemResponse> getMyCart()
    {
        var SecurityContext = SecurityContextHolder.getContext();
        String username = SecurityContext.getAuthentication().getName();
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        Cart cart = customer.getCart();
        List<CartItemResponse> cartItemResponses = new ArrayList<>();
        for(CartItem item : cart.getCartItemList())
        {
            cartItemResponses.add(
                    CartItemResponse.builder()
                            .product(item.getProduct().toProductResponse())
                            .quantity(item.getQuantity())
                            .build()
            );
        }
        return cartItemResponses;
    }

    public CustomerResponse getMyInfo()
    {
        var SecurityContext = SecurityContextHolder.getContext();
        String username = SecurityContext.getAuthentication().getName();

        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        return customerToCustomerResponse(customer);
    }

    public ReceiptResponse order()
    {
        var SecurityContext = SecurityContextHolder.getContext();
        String username = SecurityContext.getAuthentication().getName();
        Customer customer = customerRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
        Receipt myReceipt = new Receipt();
        myReceipt.setTotalPrice(0);
        customer.addReceipt(myReceipt);
        for(CartItem item : customer.getCart().getCartItemList())
        {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            if(checkIsAvailableInStock(product, quantity))
            {
                ReceiptProduct receiptProduct = new ReceiptProduct(myReceipt, product, quantity);
                myReceipt.addReceiptProduct(receiptProduct);
                myReceipt.setTotalPrice(myReceipt.getTotalPrice() + product.getPrice() * quantity);

                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
            }
            else
                throw new RuntimeException("The product_name: " + product.getName() + " has quantity is over the stock quantiy of this product!");
        }
        //xoa trong cart
        for (CartItem item : customer.getCart().getCartItemList())
        {
            cartItemRepository.deleteAll(customer.getCart().getCartItemList());
        }
        myReceipt.setBill_date(LocalDate.now());
        myReceipt.setBill_time(LocalTime.now());

        ReceiptResponse receiptResponse = new ReceiptResponse();
        receiptResponse.setProductResponseList(new ArrayList<>());

        for(ReceiptProduct receiptProduct : myReceipt.getReceiptProducts())
        {
            ProductResponse productResponse = receiptProduct.getProduct().toProductResponse();
            productResponse.setQuantity(receiptProduct.getQuantity());
            receiptResponse.getProductResponseList().add(productResponse);
        }
        receiptResponse.setTotalPrice(myReceipt.getTotalPrice());
        receiptResponse.setDate(myReceipt.getBill_date());
        receiptResponse.setTime(myReceipt.getBill_time());

        CustomerResponse customerResponse = customerToCustomerResponse(customer);
        receiptResponse.setCustomerResponse(customerResponse);

        return receiptResponse;
    }
    private boolean checkIsAvailableInStock(Product product, int quantity)
    {
        return product.getStockQuantity() >= quantity;
    }


}
