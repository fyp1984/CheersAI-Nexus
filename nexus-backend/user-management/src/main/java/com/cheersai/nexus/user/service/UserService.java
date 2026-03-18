package com.cheersai.nexus.user.service;

import com.cheersai.nexus.model.usermanagement.User;
import com.mybatisflex.core.paginate.Page;

public interface UserService {
    Page<User> getAllUsers(int pageNumber, int pageSize);
    Page<User> getAllUsers(int pageNumber, int pageSize, int totalRow);
}
