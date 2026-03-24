package com.cheersai.nexus.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 32, message = "用户名长度需为2-32位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,20}$", message = "密码需为8-20位且包含大小写字母和数字")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;

    @JsonIgnore
    @AssertTrue(message = "请提供邮箱或手机号")
    public boolean isIdentityProvided() {
        return hasText(email) || hasText(phone);
    }

    @JsonIgnore
    @AssertTrue(message = "邮箱和手机号只能填写一项")
    public boolean isSingleIdentity() {
        return !(hasText(email) && hasText(phone));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
