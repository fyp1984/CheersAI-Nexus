package com.cheersai.nexus.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName:UserDTO
 * @Description:TODO
 * @Author:userSigma
 * @CreateDate:2026/3/18 20:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageUserDTO {
    private int pageNumber;
    private int pageSize;
    private int totalRow;
}
