package com.cheersai.nexus.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeRequest {
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码用途不能为空")
    @Pattern(regexp = "^(register|login|reset_password|bind)$", message = "验证码用途不支持")
    private String purpose; // register, login, reset_password, bind

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
