package com.cheersai.nexus.membership.service;

import com.cheersai.nexus.membership.dto.PlanAuditDTO;
import com.cheersai.nexus.membership.dto.PlanBenefitsDTO;
import com.cheersai.nexus.membership.dto.PlanCreateDTO;
import com.cheersai.nexus.membership.dto.PlanDetailDTO;
import com.cheersai.nexus.membership.dto.PlanUpdateDTO;
import com.cheersai.nexus.membership.entity.MembershipPlan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 会员计划业务逻辑接口
 */
public interface PlanService extends IService<MembershipPlan>{

    /**
     * 获取会员计划列表
     */
    List<PlanDetailDTO> getPlanList();

    /**
     * 获取会员计划详情
     */
    PlanDetailDTO getPlanByCode(String code);

    /**
     * 创建会员计划（创建后进入待审批状态）
     */
    void createPlan(PlanCreateDTO dto, String applicantId, String applicantName) throws JsonProcessingException;

    /**
     * 更新会员计划（更新后进入待审批状态）
     */
    void updatePlan(String code, PlanUpdateDTO dto, String applicantId, String applicantName) throws JsonProcessingException;

    /**
     * 删除会员计划
     */
    void deletePlan(String code, String operatorId, String operatorName);

    /**
     * 审批会员计划变更
     */
    void auditPlan(String code, PlanAuditDTO dto, String auditorId, String auditorName);

    /**
     * 获取待审批的变更记录
     */
    Object getPendingAudit(String code);

    /**
     * 获取会员计划权益配置
     */
    PlanBenefitsDTO getPlanBenefits(String code);

    /**
     * 更新会员计划权益配置
     */
    void updatePlanBenefits(String code, PlanBenefitsDTO dto, String operatorId, String operatorName) throws JsonProcessingException;

    /**
     * 获取会员计划操作日志
     */
    Map<String, Object> getPlanAuditLogs(String code);
}
