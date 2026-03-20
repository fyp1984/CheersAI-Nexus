package com.cheersai.nexus.user.service;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface UserService {
    Page<User> getAllUsers(int pageNumber, int pageSize, int totalRow);
    User getUserInfoById(String userId);
    User updateUserStatus(String userId, String status);
    List<User> searchUsers(String userCondition);
}
