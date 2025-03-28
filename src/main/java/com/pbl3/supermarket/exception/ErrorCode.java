package com.pbl3.supermarket.exception;

public enum ErrorCode {
    USERNAME_NOT_FOUND(1001, "USERNAME NOT FOUND"),
    UNAUTHORIZED(1002, "UNAUTHORIZED"),
    UNAUTHENTICATED(1003, "UNAUTHENTICATED"),
    USERNAME_IS_EXISTED(1003, "USERNAME IS EXISTED"),
    CUSTOMER_ID_NOTFOUND(1004, "CUSTOMER ID NOT FOUND"),
    SUPPLIER_ID_NOTFOUND(1005, "SUPPLIER ID NOT FOUND"),
    PRODUCT_ID_NOTFOUND(1006, "PRODUCT ID NOT FOUND"),
    PRODUCT_STILL_AVAILABLE(1007, "PRODUCT STILL AVAILABLE"),
    CATEGORY_ID_NOTFOUND(1008, "CATEGORY ID NOT FOUND"),
    CUSTOMER_NAME_NOTFOUND(1009, "CUSTOMER NAME NOT FOUND"),
    PRODUCT_NOT_AVAILABLE_IN_STOCK(1010, "PRODUCT NOT AVAILABLE IN STOCK"),
    SUPPLIER_NAME_EXISTED(1011, "SUPPLIER NAME EXISTED"),
    PRODUCT_NAME_EXISTED(1012, "PRODUCT NAME EXISTED"),
    INVALIDATED_TOKEN(1013, "INVALIDATED TOKEN"),
    ;
    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    };
    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
