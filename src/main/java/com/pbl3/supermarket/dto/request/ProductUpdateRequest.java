package com.pbl3.supermarket.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ProductUpdateRequest {
    String name;
    String imageUrl;
    int stockQuantity;
    float price;
    String unit_price;
    float discount;
    LocalDate createDate;
    LocalDate expiryDate;

    Long newCategoryId;
    String newSupplierId;
}
