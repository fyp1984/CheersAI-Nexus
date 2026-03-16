package com.cheersai.nexus.user.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {

    ACTIVE("active", "正常"),
    INACTIVE("inactive", "未激活"),
    DELETED("deleted", "已删除");

    private final String value;
    private final String desc;

    UserStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static UserStatus fromValue(String value) {
        for (UserStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
