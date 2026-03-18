package com.cheersai.nexus.common.model.base;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName:SysUser
 * @Description:系统用户类（暂定）
 * @Author:userSigma
 * @CreateDate:2026/3/17 21:22
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("sys_user")
public class SysUser {
    @Id
    private String userId;
    private String userName;
    private String nickName;
    private String password;
    private String email;
    private String phone;
}
