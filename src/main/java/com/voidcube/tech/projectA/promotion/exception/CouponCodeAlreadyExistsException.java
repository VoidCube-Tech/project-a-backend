package com.voidcube.tech.projectA.promotion.exception;

public class CouponCodeAlreadyExistsException extends RuntimeException {

    public CouponCodeAlreadyExistsException(String couponCode) {
        super("O código de cupom já existe neste Tenant: " + couponCode);
    }
}
