package com.pbl3.supermarket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends User {
    int point;

    @OneToMany(mappedBy = "customer")
    List<Receipt> receipts;

    @OneToOne(mappedBy = "customer")
    private Cart cart;



    public void setCart(Cart cart) {
        this.cart = cart;
        if (cart != null) {
            cart.setCustomer(this);
        }
    }

    public void addReceipt(Receipt receipt) {
        receipts.add(receipt);
        if(receipt != null) {
            receipt.setCustomer(this);
        }
    }
}
