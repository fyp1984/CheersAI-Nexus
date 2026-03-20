package com.cheersai.nexus.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("verification_codes")
public class VerificationCode {

    @Id
    private String id;

    private String target;  // email or phone

    private String code;

    private String type;    // email, phone

    private String purpose; // register, login, reset_password, bind

    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean used = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
