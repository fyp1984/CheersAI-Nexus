package com.cheersai.nexus.user.service;

import com.cheersai.nexus.user.dto.ResetPasswordResponseDTO;
import com.cheersai.nexus.user.dto.BetaApplyRequestDTO;
import com.cheersai.nexus.user.dto.UserCreateDTO;
import com.cheersai.nexus.user.dto.UserListQueryDTO;
import com.cheersai.nexus.user.dto.UserListResponseDTO;
import com.cheersai.nexus.user.dto.UserRecordDTO;
import com.cheersai.nexus.user.dto.UserStatusBatchUpdateDTO;
import com.cheersai.nexus.user.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    UserListResponseDTO getUsers(UserListQueryDTO queryDTO);

    List<UserRecordDTO> searchUsers(String userCondition);

    UserRecordDTO getUserInfoById(String userId);

    UserRecordDTO createUser(UserCreateDTO dto);

    UserRecordDTO updateUser(String userId, UserUpdateDTO dto);

    UserRecordDTO updateUserStatus(String userId, String status);

    void updateBatchStatus(UserStatusBatchUpdateDTO dto);

    ResetPasswordResponseDTO resetPassword(String userId);

    UserRecordDTO applyBeta(BetaApplyRequestDTO dto);
}

