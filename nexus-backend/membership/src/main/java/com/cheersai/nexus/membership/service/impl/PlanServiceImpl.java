package com.cheersai.nexus.membership.service.impl;

import com.cheersai.nexus.common.utils.JacksonUtils;
import com.cheersai.nexus.membership.dto.PlanAuditDTO;
import com.cheersai.nexus.membership.dto.PlanBenefitsDTO;
import com.cheersai.nexus.membership.dto.PlanCreateDTO;
import com.cheersai.nexus.membership.dto.PlanDetailDTO;
import com.cheersai.nexus.membership.dto.PlanUpdateDTO;
import com.cheersai.nexus.membership.entity.MembershipPlan;
import com.cheersai.nexus.membership.entity.PlanAuditLog;
import com.cheersai.nexus.membership.mapper.MembershipPlanMapper;
import com.cheersai.nexus.membership.mapper.PlanAuditLogMapper;
import com.cheersai.nexus.membership.service.PlanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cheersai.nexus.membership.entity.table.MembershipPlanTableDef.MEMBERSHIP_PLAN;

/**
 * 会员计划业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class PlanServiceImpl extends ServiceImpl<MembershipPlanMapper, MembershipPlan> implements PlanService {

    @Autowired
    private final PlanAuditLogMapper planAuditLogMapper;
    
    @Autowired
    private final JacksonUtils jacksonUtils;

    @Override
    public List<PlanDetailDTO> getPlanList() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(MEMBERSHIP_PLAN)
                .orderBy(MEMBERSHIP_PLAN.SORT_ORDER.asc());

        List<MembershipPlan> plans = this.list(queryWrapper);

        return plans.stream().map(this::toPlanDetailDTO).collect(Collectors.toList());
    }

    @Override
    public PlanDetailDTO getPlanByCode(String code) {
        MembershipPlan plan = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (plan == null) {
            return null;
        }

        return toPlanDetailDTO(plan);
    }

    @Override
    @Transactional
    public void createPlan(PlanCreateDTO dto, String applicantId, String applicantName) throws JsonProcessingException {
        // 检查编码是否已存在
        MembershipPlan existing = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(dto.getCode())));
        
        if (existing != null) {
            throw new RuntimeException("计划编码已存在");
        }

        // 创建计划（初始状态由前端控制，后端只保存）
        MembershipPlan plan = MembershipPlan.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .priceMonthly(dto.getPriceMonthly())
                .priceYearly(dto.getPriceYearly())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "CNY")
                .features(dto.getFeatures())
                .limits(dto.getLimits())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : "active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        this.save(plan);

        // 创建审批记录（新创建默认通过，无需审批）
        createAuditLog(
                plan.getId(),
                "create", 
                "approved", 
                null, 
                null, 
                null, 
                applicantId, 
                applicantName, 
                "系统自动审批"
        );
    }

    @Override
    @Transactional
    public void updatePlan(String code, PlanUpdateDTO dto, String applicantId, String applicantName) throws JsonProcessingException {
        MembershipPlan existing = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (existing == null) {
            throw new RuntimeException("计划不存在");
        }

        // 记录变更前的数据
        String beforeData = jacksonUtils.toJson(existing);

        // 构建更新
        UpdateChain<MembershipPlan> updateChain = UpdateChain.of(MembershipPlan.class)
                .set(MEMBERSHIP_PLAN.UPDATED_AT, LocalDateTime.now());

        if (StringUtils.hasText(dto.getName())) {
            updateChain.set(MEMBERSHIP_PLAN.NAME, dto.getName());
        }
        if (dto.getDescription() != null) {
            updateChain.set(MEMBERSHIP_PLAN.DESCRIPTION, dto.getDescription());
        }
        if (dto.getPriceMonthly() != null) {
            updateChain.set(MEMBERSHIP_PLAN.PRICE_MONTHLY, dto.getPriceMonthly());
        }
        if (dto.getPriceYearly() != null) {
            updateChain.set(MEMBERSHIP_PLAN.PRICE_YEARLY, dto.getPriceYearly());
        }
        if (StringUtils.hasText(dto.getCurrency())) {
            updateChain.set(MEMBERSHIP_PLAN.CURRENCY, dto.getCurrency());
        }
        if (dto.getFeatures() != null) {
            updateChain.set(MEMBERSHIP_PLAN.FEATURES, dto.getFeatures());
        }
        if (dto.getLimits() != null) {
            updateChain.set(MEMBERSHIP_PLAN.LIMITS, dto.getLimits());
        }
        if (dto.getSortOrder() != null) {
            updateChain.set(MEMBERSHIP_PLAN.SORT_ORDER, dto.getSortOrder());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            updateChain.set(MEMBERSHIP_PLAN.STATUS, dto.getStatus());
        }

        // 执行更新
        updateChain.where(MEMBERSHIP_PLAN.CODE.eq(code)).update();

        // 获取更新后的数据
        MembershipPlan updated = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        String afterData = jacksonUtils.toJson(updated);

        // 创建审批记录（待审批状态）
        createAuditLog(existing.getId(), "update", "pending", beforeData, afterData,
                dto.getApplyRemark(),
                applicantId, applicantName, null);
    }

    @Override
    @Transactional
    public void deletePlan(String code, String operatorId, String operatorName) {
        MembershipPlan existing = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (existing == null) {
            throw new RuntimeException("计划不存在");
        }

        // 记录变更前的数据
        String beforeData;
        try {
            beforeData = jacksonUtils.toJson(existing);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 逻辑删除（改为disabled状态）
        UpdateChain.of(MembershipPlan.class)
                .set(MEMBERSHIP_PLAN.STATUS, "disabled")
                .set(MEMBERSHIP_PLAN.UPDATED_AT, LocalDateTime.now())
                .where(MEMBERSHIP_PLAN.CODE.eq(code))
                .update();

        // 创建审批记录
        createAuditLog(existing.getId(), "delete", "approved", beforeData, null,
                "删除计划",
                operatorId, operatorName, "系统自动审批");
    }

    @Override
    @Transactional
    public void auditPlan(String code, PlanAuditDTO dto, String auditorId, String auditorName) {
        MembershipPlan plan = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }

        // 获取最新的待审批记录
        PlanAuditLog pendingLog = planAuditLogMapper.selectOneByQuery(QueryWrapper.create()
                .from(PlanAuditLog.class)
                .where(PlanAuditLog::getPlanId).eq(plan.getId())
                .and(PlanAuditLog::getAuditStatus).eq("pending")
                .orderBy("created_at", false)
                .limit(1));

        if (pendingLog == null) {
            throw new RuntimeException("没有待审批的变更记录");
        }

        // 执行审批操作
        String auditStatus = "approve".equals(dto.getAction()) ? "approved" : "rejected";
        
        // 更新审批记录
        UpdateChain.of(PlanAuditLog.class)
                .set(PlanAuditLog::getAuditStatus, auditStatus)
                .set(PlanAuditLog::getAuditorId, auditorId)
                .set(PlanAuditLog::getAuditorName, auditorName)
                .set(PlanAuditLog::getAuditRemark, dto.getAuditRemark())
                .set(PlanAuditLog::getAuditedAt, LocalDateTime.now())
                .eq(PlanAuditLog::getId, pendingLog.getId())
                .update();

        // 如果审批通过，应用变更
        if ("approved".equals(auditStatus)) {
            // 根据操作类型处理
            if ("delete".equals(pendingLog.getOperateType())) {
                // 删除操作：确保状态已更新
                UpdateChain.of(MembershipPlan.class)
                        .set(MEMBERSHIP_PLAN.STATUS, "disabled")
                        .set(MEMBERSHIP_PLAN.UPDATED_AT, LocalDateTime.now())
                        .where(MEMBERSHIP_PLAN.CODE.eq(code))
                        .update();
            }
            // update操作已经在更新时应用了，这里不需要额外处理
        }
        // 如果驳回，变更自动废止，不需要额外处理
    }

    @Override
    public Object getPendingAudit(String code) {
        MembershipPlan plan = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (plan == null) {
            return null;
        }

        return planAuditLogMapper.selectOneByQuery(QueryWrapper.create()
                .from(PlanAuditLog.class)
                .where(PlanAuditLog::getPlanId).eq(plan.getId())
                .and(PlanAuditLog::getAuditStatus).eq("pending")
                .orderBy("created_at", false)
                .limit(1));
    }

    @Override
    public PlanBenefitsDTO getPlanBenefits(String code) {
        MembershipPlan plan = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (plan == null) {
            return null;
        }

        List<PlanBenefitsDTO.BenefitItem> benefits = parseBenefits(plan.getFeatures());
        
        return PlanBenefitsDTO.builder()
                .planCode(plan.getCode())
                .planName(plan.getName())
                .benefits(benefits)
                .build();
    }

    @Override
    @Transactional
    public void updatePlanBenefits(String code, PlanBenefitsDTO dto, String operatorId, String operatorName) throws JsonProcessingException {
        MembershipPlan existing = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (existing == null) {
            throw new RuntimeException("计划不存在");
        }

        // 序列化为 JSON 字符串
        String featuresJson = jacksonUtils.toJson(dto.getBenefits());
        
        // 记录变更前的数据
        String beforeData = jacksonUtils.toJson(existing);

        // 更新 features 字段
        UpdateChain.of(MembershipPlan.class)
                .set(MEMBERSHIP_PLAN.FEATURES, featuresJson)
                .set(MEMBERSHIP_PLAN.UPDATED_AT, LocalDateTime.now())
                .where(MEMBERSHIP_PLAN.CODE.eq(code))
                .update();

        // 获取更新后的数据
        MembershipPlan updated = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        String afterData = jacksonUtils.toJson(updated);

        // 创建权益配置变更的审批记录
        createAuditLog(existing.getId(), "benefit", "approved", beforeData, afterData,
                "权益配置更新",
                operatorId, operatorName, "系统自动审批");
    }

    @Override
    public Map<String, Object> getPlanAuditLogs(String code) {
        MembershipPlan plan = this.getOne(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(code)));

        if (plan == null) {
            return null;
        }

        List<PlanAuditLog> logs = planAuditLogMapper.selectListByQuery(QueryWrapper.create()
                .from(PlanAuditLog.class)
                .where(PlanAuditLog::getPlanId).eq(plan.getId())
                .orderBy("created_at desc")
                .limit(100));

        Map<String, Object> result = new HashMap<>();
        result.put("planCode", code);
        result.put("planName", plan.getName());
        result.put("items", logs);
        result.put("total", logs.size());
        return result;
    }

    /**
     * 解析 benefits JSON 字符串为 List
     */
    private List<PlanBenefitsDTO.BenefitItem> parseBenefits(String featuresJson) {
        if (!StringUtils.hasText(featuresJson)) {
            return java.util.Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(featuresJson,
                    new TypeReference<>() {
                    });
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private PlanDetailDTO toPlanDetailDTO(MembershipPlan plan) {
        // 获取最新的审批状态
        String auditStatus = getLatestAuditStatus(plan.getId());
        
        return PlanDetailDTO.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .priceMonthly(plan.getPriceMonthly())
                .priceYearly(plan.getPriceYearly())
                .currency(plan.getCurrency())
                .features(plan.getFeatures())
                .limits(plan.getLimits())
                .sortOrder(plan.getSortOrder())
                .status(plan.getStatus())
                .auditStatus(auditStatus)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private String getLatestAuditStatus(String planId) {
        PlanAuditLog latestLog = planAuditLogMapper.selectOneByQuery(QueryWrapper.create()
                .from(PlanAuditLog.class)
                .where(PlanAuditLog::getPlanId).eq(planId)
                .orderBy("created_at", false)
                .limit(1));
        
        return latestLog != null ? latestLog.getAuditStatus() : null;
    }

    private void createAuditLog(String planId, String operateType, String auditStatus,
                                 String beforeData, String afterData,
                                String applyRemark,
                                 String applicantId, String applicantName,
                                String auditRemark) {
        PlanAuditLog log = PlanAuditLog.builder()
                .planId(planId)
                .operateType(operateType)
                .auditStatus(auditStatus)
                .beforeData(beforeData)
                .afterData(afterData)
                .applicantId(applicantId)
                .applicantName(applicantName)
                .applyRemark(applyRemark)
                .auditRemark(auditRemark)
                .appliedAt(LocalDateTime.now())
                .auditedAt("approved".equals(auditStatus) ? LocalDateTime.now() : null)
                .createdAt(LocalDateTime.now())
                .build();

        planAuditLogMapper.insert(log);
    }
}
