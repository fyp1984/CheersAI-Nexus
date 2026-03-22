package com.cheersai.nexus.common.model.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Input {
    private String Encryption;//加密方式
    private String Sign; // 签名
    private String Noise;//随机数
    private String FunctionID;//功能函數
    private String Token;    //令牌
    private String Data;//入口参数
}
