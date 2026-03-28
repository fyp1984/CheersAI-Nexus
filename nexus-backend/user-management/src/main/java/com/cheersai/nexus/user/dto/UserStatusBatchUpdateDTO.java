package com.cheersai.nexus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusBatchUpdateDTO {

    @NotEmpty(message = "用户ID列表不能为空")
    private List<String> userIds;

    @NotBlank(message = "状态不能为空")
    private String status;

    public List<String> getIds() {
        return this.userIds;
    }
}

