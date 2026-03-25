package com.cheersai.nexus.product.exception;

import lombok.Getter;

@Getter
public class ProductBusinessException extends RuntimeException {

    private final int code;

    public ProductBusinessException(ProductErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
    }

    public ProductBusinessException(ProductErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
