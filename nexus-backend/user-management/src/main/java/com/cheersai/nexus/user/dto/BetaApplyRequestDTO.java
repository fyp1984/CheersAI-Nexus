package com.cheersai.nexus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetaApplyRequestDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过255")
    private String email;

    @Size(max = 20, message = "语言标识长度不能超过20")
    private String language;
}
