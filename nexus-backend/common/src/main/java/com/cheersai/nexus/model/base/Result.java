package com.cheersai.nexus.model.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Result {
    private String Status; // 是否成功
    private String Info; // 返回值
    private String Encrypt; // 加密方式
    private String ErrCode; // 错误码
    private String Data;  // 错误信息
}
