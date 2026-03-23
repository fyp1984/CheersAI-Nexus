package com.cheersai.nexus.membership.service;

import com.cheersai.nexus.membership.dto.SubscriptionAdjustDTO;
import com.cheersai.nexus.membership.dto.SubscriptionCreateDTO;
import com.cheersai.nexus.membership.dto.SubscriptionDetailDTO;
import com.cheersai.nexus.membership.dto.UserSubscriptionDTO;
import com.cheersai.nexus.membership.entity.Subscription;
import com.cheersai.nexus.membership.entity.SubscriptionAuditLog;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 用户订阅业务逻辑接口
 */
public interface SubscriptionService extends IService<Subscription>{

    /**
     * 获取订阅列表
     */
    List<SubscriptionDetailDTO> getSubscriptionList(String userId, String status);

    /**
     * 获取订阅详情
     */
    SubscriptionDetailDTO getSubscriptionById(String id);

    /**
     * 获取用户当前有效订阅
     */
    UserSubscriptionDTO getUserSubscription(String userId);

    /**
     * 创建订阅
     */
    SubscriptionDetailDTO createSubscription(SubscriptionCreateDTO dto);

    /**
     * 更新订阅
     */
    void updateSubscription(String id, SubscriptionCreateDTO dto);

    /**
     * 手动调整用户会员（运营操作）
     */
    UserSubscriptionDTO adjustUserSubscription(String userId, SubscriptionAdjustDTO dto, 
                                                String operatorId, String operatorName, String operatorIp);

    /**
     * 获取用户订阅变更审计日志
     */
    List<SubscriptionAuditLog> getSubscriptionAuditLogs(String userId);
}
