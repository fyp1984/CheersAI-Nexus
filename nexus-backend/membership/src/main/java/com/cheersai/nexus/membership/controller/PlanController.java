package com.cheersai.nexus.membership.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.membership.dto.PlanAuditDTO;
import com.cheersai.nexus.membership.dto.PlanBenefitsDTO;
import com.cheersai.nexus.membership.dto.PlanCreateDTO;
import com.cheersai.nexus.membership.dto.PlanDetailDTO;
import com.cheersai.nexus.membership.dto.PlanUpdateDTO;
import com.cheersai.nexus.membership.entity.PlanAuditLog;
import com.cheersai.nexus.membership.service.PlanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员计划管理控制器
 */
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /**
     * 获取会员计划列表
     */
    @GetMapping
    public Result<List<PlanDetailDTO>> getPlanList() {
        return Result.success(planService.getPlanList());
    }

    /**
     * 获取会员计划详情
     */
    @GetMapping("/{code}")
    public Result<PlanDetailDTO> getPlanDetail(@PathVariable("code") String code) {
        PlanDetailDTO plan = planService.getPlanByCode(code);
        if (plan == null) {
            return Result.error("会员计划不存在");
        }
        return Result.success(plan);
    }

    /**
     * 创建会员计划
     */
    @PostMapping
    public Result<Void> createPlan(@RequestBody PlanCreateDTO dto,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId,
                                   @RequestHeader(value = "X-User-Name", required = false) String userName) {
        // 简化处理：使用默认值
        String applicantId = userId != null ? userId : "system";
        String applicantName = userName != null ? userName : "系统";

        try {
            planService.createPlan(dto, applicantId, applicantName);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 更新会员计划
     */
    @PutMapping("/{code}")
    public Result<Void> updatePlan(@PathVariable("code") String code,
                                    @RequestBody PlanUpdateDTO dto,
                                    @RequestHeader(value = "X-User-Id", required = false) String userId,
                                    @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String applicantId = userId != null ? userId : "system";
        String applicantName = userName != null ? userName : "系统";

        try {
            planService.updatePlan(code, dto, applicantId, applicantName);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 删除会员计划
     */
    @DeleteMapping("/{code}")
    public Result<Void> deletePlan(@PathVariable("code") String code,
                                     @RequestHeader(value = "X-User-Id", required = false) String userId,
                                     @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String operatorId = userId != null ? userId : "system";
        String operatorName = userName != null ? userName : "系统";
        
        planService.deletePlan(code, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 审批会员计划变更
     */
    @PostMapping("/{code}/audit")
    public Result<Void> auditPlan(@PathVariable("code") String code,
                                   @RequestBody PlanAuditDTO dto,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId,
                                   @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String auditorId = userId != null ? userId : "admin";
        String auditorName = userName != null ? userName : "管理员";
        
        planService.auditPlan(code, dto, auditorId, auditorName);
        return Result.success();
    }

    /**
     * 获取待审批的变更记录
     */
    @GetMapping("/{code}/pending-audit")
    public Result<Object> getPendingAudit(@PathVariable("code") String code) {
        return Result.success(planService.getPendingAudit(code));
    }

    /**
     * 获取会员计划权益配置
     */
    @GetMapping("/{code}/benefits")
    public Result<PlanBenefitsDTO> getPlanBenefits(@PathVariable("code") String code) {
        PlanBenefitsDTO benefits = planService.getPlanBenefits(code);
        if (benefits == null) {
            return Result.error("会员计划不存在");
        }
        return Result.success(benefits);
    }

    /**
     * 更新会员计划权益配置
     */
    @PutMapping("/{code}/benefits")
    public Result<Void> updatePlanBenefits(@PathVariable("code") String code,
                                          @RequestBody PlanBenefitsDTO dto,
                                          @RequestHeader(value = "X-User-Id", required = false) String userId,
                                          @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String operatorId = userId != null ? userId : "system";
        String operatorName = userName != null ? userName : "系统";
        
        try {
            planService.updatePlanBenefits(code, dto, operatorId, operatorName);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 获取会员计划操作日志
     */
    @GetMapping("/{code}/audit-logs")
    public Result<Map<String, Object>> getPlanAuditLogs(@PathVariable("code") String code) {
        return Result.success(planService.getPlanAuditLogs(code));
    }
}
