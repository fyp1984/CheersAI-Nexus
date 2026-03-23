package com.cheersai.nexus.membership.service.impl;

import com.cheersai.nexus.membership.dto.SubscriptionAdjustDTO;
import com.cheersai.nexus.membership.dto.SubscriptionCreateDTO;
import com.cheersai.nexus.membership.dto.SubscriptionDetailDTO;
import com.cheersai.nexus.membership.dto.UserSubscriptionDTO;
import com.cheersai.nexus.membership.entity.MembershipPlan;
import com.cheersai.nexus.membership.entity.Subscription;
import com.cheersai.nexus.membership.entity.SubscriptionAuditLog;
import com.cheersai.nexus.membership.mapper.MembershipPlanMapper;
import com.cheersai.nexus.membership.mapper.SubscriptionAuditLogMapper;
import com.cheersai.nexus.membership.mapper.SubscriptionMapper;
import com.cheersai.nexus.membership.service.SubscriptionService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.cheersai.nexus.membership.entity.table.MembershipPlanTableDef.MEMBERSHIP_PLAN;
import static com.cheersai.nexus.membership.entity.table.SubscriptionTableDef.SUBSCRIPTION;

/**
 * 用户订阅业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    @Autowired
    private final MembershipPlanMapper membershipPlanMapper;
    
    @Autowired
    private final SubscriptionAuditLogMapper subscriptionAuditLogMapper;
    
    @Autowired
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public List<SubscriptionDetailDTO> getSubscriptionList(String userId, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(SUBSCRIPTION);

        if (StringUtils.hasText(userId)) {
            queryWrapper.where(SUBSCRIPTION.USER_ID.eq(userId));
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.where(SUBSCRIPTION.STATUS.eq(status));
        }

        queryWrapper.orderBy(SUBSCRIPTION.CREATED_AT.desc());

        List<Subscription> subscriptions = subscriptionMapper.selectListByQuery(queryWrapper);
        
        if (subscriptions == null || subscriptions.isEmpty()) {
            throw new RuntimeException("Null subscriptions!");
        }
        
        return subscriptions.stream()
                .map(this::toSubscriptionDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDetailDTO getSubscriptionById(String id) {
        Subscription subscription = subscriptionMapper.selectOneById(id);
        if (subscription == null) {
            return null;
        }
        return toSubscriptionDetailDTO(subscription);
    }

    @Override
    public UserSubscriptionDTO getUserSubscription(String userId) {
        // 获取用户当前有效的订阅
        Subscription subscription = subscriptionMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(SUBSCRIPTION)
                        .where(SUBSCRIPTION.USER_ID.eq(userId))
                        .and(SUBSCRIPTION.STATUS.eq("active"))
                        .orderBy(SUBSCRIPTION.END_DATE.desc())
                        .limit(1)
        );

        if (subscription == null) {
            // 返回免费版信息
            return UserSubscriptionDTO.builder()
                    .userId(userId)
                    .currentPlanCode("free")
                    .currentPlanName("Free")
                    .subscriptionStatus("inactive")
                    .build();
        }

        // 获取计划信息
        MembershipPlan plan = membershipPlanMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(MEMBERSHIP_PLAN)
                        .where(MEMBERSHIP_PLAN.CODE.eq(subscription.getPlanCode()))
        );

        return UserSubscriptionDTO.builder()
                .userId(subscription.getUserId())
                .subscriptionId(subscription.getId())
                .currentPlanCode(subscription.getPlanCode())
                .currentPlanName(plan != null ? plan.getName() : subscription.getPlanCode())
                .subscriptionStatus(subscription.getStatus())
                .planExpire(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .build();
    }

    @Override
    @Transactional
    public SubscriptionDetailDTO createSubscription(SubscriptionCreateDTO dto) {
        // 检查计划是否存在
        MembershipPlan plan = membershipPlanMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(MEMBERSHIP_PLAN)
                        .where(MEMBERSHIP_PLAN.CODE.eq(dto.getPlanCode()))
        );

        if (plan == null) {
            throw new RuntimeException("会员计划不存在");
        }

        // 先取消用户的所有有效订阅
        UpdateChain.of(Subscription.class)
                .set(SUBSCRIPTION.STATUS, "cancelled")
                .set(SUBSCRIPTION.UPDATED_AT, LocalDateTime.now())
                .where(SUBSCRIPTION.USER_ID.eq(dto.getUserId()))
                .and(SUBSCRIPTION.STATUS.eq("active"))
                .update();

        // 创建新订阅
        Subscription subscription = Subscription.builder()
                .userId(dto.getUserId())
                .planCode(dto.getPlanCode())
                .status("active")
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .autoRenew(dto.getAutoRenew() != null ? dto.getAutoRenew() : false)
                .paymentMethod(dto.getPaymentMethod())
                .lastPaymentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        this.save(subscription);

        return toSubscriptionDetailDTO(subscription);
    }

    @Override
    @Transactional
    public void updateSubscription(String id, SubscriptionCreateDTO dto) {
        Subscription existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("订阅不存在");
        }

        // 更新订阅信息
        UpdateChain.of(Subscription.class)
                .set(SUBSCRIPTION.PLAN_CODE, dto.getPlanCode())
                .set(SUBSCRIPTION.STATUS, dto.getStatus() != null ? dto.getStatus() : existing.getStatus())
                .set(SUBSCRIPTION.START_DATE, dto.getStartDate())
                .set(SUBSCRIPTION.END_DATE, dto.getEndDate())
                .set(SUBSCRIPTION.AUTO_RENEW, dto.getAutoRenew())
                .set(SUBSCRIPTION.PAYMENT_METHOD, dto.getPaymentMethod())
                .set(SUBSCRIPTION.UPDATED_AT, LocalDateTime.now())
                .where(SUBSCRIPTION.ID.eq(id))
                .update();
    }

    @Override
    @Transactional
    public UserSubscriptionDTO adjustUserSubscription(String userId, SubscriptionAdjustDTO dto,
                                                        String operatorId, String operatorName, String operatorIp) {
        // 获取当前有效订阅
        Subscription currentSubscription = this.getOne(QueryWrapper.create()
                .from(SUBSCRIPTION)
                .where(SUBSCRIPTION.USER_ID.eq(userId))
                .and(SUBSCRIPTION.STATUS.eq("active"))
                .orderBy(SUBSCRIPTION.END_DATE.desc())
                .limit(1));

        String beforePlanCode = currentSubscription != null ? currentSubscription.getPlanCode() : "free";
        String beforeEndDate = currentSubscription != null ? currentSubscription.getEndDate().toString() : null;

        // 检查目标计划是否存在
        MembershipPlan targetPlan = membershipPlanMapper.selectOneByQuery(QueryWrapper.create()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(dto.getPlanCode())));

        if (targetPlan == null) {
            throw new RuntimeException("目标会员计划不存在");
        }

        LocalDate newEndDate;
        if (dto.getExpireDays() == null || dto.getExpireDays() <= 0) {
            // 永久有效
            newEndDate = LocalDate.now().plusYears(100);
        } else {
            // 计算新的结束日期
            newEndDate = LocalDate.now().plusDays(dto.getExpireDays());
        }

        String afterPlanCode = dto.getPlanCode();
        String afterEndDate = newEndDate.toString();

        // 确定操作类型
        String operateType = determineOperateType(beforePlanCode, dto.getPlanCode());

        if (currentSubscription != null) {
            // 更新现有订阅
            UpdateChain.of(Subscription.class)
                    .set(SUBSCRIPTION.PLAN_CODE, dto.getPlanCode())
                    .set(SUBSCRIPTION.END_DATE, newEndDate)
                    .set(SUBSCRIPTION.UPDATED_AT, LocalDateTime.now())
                    .where(SUBSCRIPTION.ID.eq(currentSubscription.getId()))
                    .update();

            // 获取更新后的订阅
            currentSubscription = this.getById(currentSubscription.getId());
        } else {
            // 创建新订阅
            Subscription newSubscription = Subscription.builder()
                    .userId(userId)
                    .planCode(dto.getPlanCode())
                    .status("active")
                    .startDate(LocalDate.now())
                    .endDate(newEndDate)
                    .autoRenew(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            this.save(newSubscription);
            currentSubscription = newSubscription;
        }

        // 记录审计日志
        SubscriptionAuditLog auditLog = SubscriptionAuditLog.builder()
                .subscriptionId(currentSubscription.getId())
                .userId(userId)
                .operateType(operateType)
                .beforePlanCode(beforePlanCode)
                .afterPlanCode(afterPlanCode)
                .beforeEndDate(beforeEndDate)
                .afterEndDate(afterEndDate)
                .reason(dto.getReason())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .operatorIp(operatorIp)
                .createdAt(LocalDateTime.now())
                .build();

        subscriptionAuditLogMapper.insert(auditLog);

        // 返回更新后的用户订阅信息
        return getUserSubscription(userId);
    }

    @Override
    public List<SubscriptionAuditLog> getSubscriptionAuditLogs(String userId) {
        return subscriptionAuditLogMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(SubscriptionAuditLog.class)
                        .where(SubscriptionAuditLog::getUserId).eq(userId)
                        .orderBy("created_at", false));
    }

    private SubscriptionDetailDTO toSubscriptionDetailDTO(Subscription subscription) {
        MembershipPlan plan = membershipPlanMapper.selectOneByQuery(QueryWrapper.create().select()
                .from(MEMBERSHIP_PLAN)
                .where(MEMBERSHIP_PLAN.CODE.eq(subscription.getPlanCode())));

        return SubscriptionDetailDTO.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .planCode(subscription.getPlanCode())
                .planName(plan != null ? plan.getName() : subscription.getPlanCode())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .paymentMethod(subscription.getPaymentMethod())
                .lastPaymentAt(subscription.getLastPaymentAt())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    private String determineOperateType(String beforePlanCode, String afterPlanCode) {
        if ("free".equals(beforePlanCode) || beforePlanCode == null) {
            return "upgrade";
        } else if ("free".equals(afterPlanCode)) {
            return "downgrade";
        }
        // 简单比较：实际上是升级还是降级
        return "adjust";
    }
}
