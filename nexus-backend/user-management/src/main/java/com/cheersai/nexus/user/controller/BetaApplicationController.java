package com.cheersai.nexus.user.controller;

import com.cheersai.nexus.user.dto.BetaApplyRequestDTO;
import com.cheersai.nexus.user.dto.UserRecordDTO;
import com.cheersai.nexus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BetaApplicationController {

    private final UserService userService;

    @PostMapping({"/nexus/api/beta-applications/apply", "/api/v1/beta-applications/apply"})
    public ResponseEntity<Map<String, Object>> apply(@RequestBody @Valid BetaApplyRequestDTO dto) {
        try {
            UserRecordDTO user = userService.applyBeta(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("result", "success");
            response.put("message", "Application submitted successfully");
            response.put("status", user.getStatus());
            response.put("userId", user.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("result", "fail");
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("result", "fail");
            response.put("message", "服务器异常，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
