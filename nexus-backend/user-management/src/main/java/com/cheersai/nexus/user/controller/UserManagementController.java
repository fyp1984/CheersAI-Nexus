package com.cheersai.nexus.user.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.user.dto.ResetPasswordResponseDTO;
import com.cheersai.nexus.user.dto.UserCreateDTO;
import com.cheersai.nexus.user.dto.UserListQueryDTO;
import com.cheersai.nexus.user.dto.UserListResponseDTO;
import com.cheersai.nexus.user.dto.UserRecordDTO;
import com.cheersai.nexus.user.dto.UserStatusBatchUpdateDTO;
import com.cheersai.nexus.user.dto.UserUpdateDTO;
import com.cheersai.nexus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;

    @GetMapping
    public Result<UserListResponseDTO> getAllUsers(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "role", required = false) String role,
                                                   @RequestParam(value = "memberPlanCode", required = false) String memberPlanCode,
                                                   @RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        UserListQueryDTO query = UserListQueryDTO.builder()
                .keyword(keyword)
                .status(status)
                .role(role)
                .memberPlanCode(memberPlanCode)
                .page(page)
                .pageSize(pageSize)
                .build();
        return Result.success(userService.getUsers(query));
    }

    @GetMapping("/search")
    public Result<List<UserRecordDTO>> searchUsers(@RequestParam("userCondition") String userCondition) {
        return Result.success(userService.searchUsers(userCondition));
    }

    @GetMapping("/{userId}")
    public Result<UserRecordDTO> getUserInfoById(@PathVariable("userId") String userId) {
        return Result.success(userService.getUserInfoById(userId));
    }

    @PostMapping
    public Result<UserRecordDTO> createUser(@RequestBody @Valid UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @PutMapping("/{userId}")
    public Result<UserRecordDTO> updateUser(@PathVariable("userId") String userId,
                                            @RequestBody @Valid UserUpdateDTO dto) {
        return Result.success(userService.updateUser(userId, dto));
    }

    @PatchMapping("/{userId}/status")
    public Result<UserRecordDTO> updateUserStatus(@PathVariable("userId") String userId,
                                                   @RequestParam("status") String status) {
        try {
            return Result.success(userService.updateUserStatus(userId, status));
        } catch (RuntimeException ex) {
            return Result.error(400, ex.getMessage());
        }
    }

    @PostMapping("/batch-status")
    public Result<Void> updateBatchStatus(@RequestBody @Valid UserStatusBatchUpdateDTO dto) {
        userService.updateBatchStatus(dto);
        return Result.success();
    }

    @PostMapping("/{userId}/reset-password")
    public Result<ResetPasswordResponseDTO> resetPassword(@PathVariable("userId") String userId) {
        return Result.success(userService.resetPassword(userId));
    }
}

