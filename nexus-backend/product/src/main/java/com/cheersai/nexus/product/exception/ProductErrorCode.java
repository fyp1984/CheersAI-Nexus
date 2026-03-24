package com.cheersai.nexus.product.exception;

import lombok.Getter;

@Getter
public enum ProductErrorCode {

    INVALID_PARAMETER(40001, "参数错误"),
    PRODUCT_NOT_FOUND(40401, "产品不存在"),
    VERSION_NOT_FOUND(40402, "版本不存在"),
    PRODUCT_CODE_EXISTS(40901, "产品编码已存在"),
    VERSION_EXISTS(40902, "版本号已存在"),
    INVALID_STATUS(40903, "无效的状态值"),
    INTERNAL_ERROR(50000, "系统开小差了，请稍后重试");

    private final int code;
    private final String defaultMessage;

    ProductErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
