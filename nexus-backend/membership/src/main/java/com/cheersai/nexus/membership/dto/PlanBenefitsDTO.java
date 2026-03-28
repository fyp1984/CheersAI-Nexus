package com.cheersai.nexus.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会员计划权益配置 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanBenefitsDTO {

    /**
     * 计划代码
     */
    private String planCode;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 权益项列表
     */
    private List<BenefitItem> benefits;

    /**
     * 权益项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitItem {
        /**
         * 权益项标识
         */
        private String key;

        /**
         * 权益项名称
         */
        private String name;

        /**
         * 权益项描述
         */
        private String description;

        /**
         * 权益类型：switch-开关, input-输入值, unlimited-无限额
         */
        private String type;

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 额度值（当type为input或unlimited时）
         */
        private Integer value;

        /**
         * 最小值
         */
        private Integer min;

        /**
         * 是否无限额（当type为unlimited时）
         */
        private Boolean unlimited;

        /**
         * 关联的会员计划代码列表
         */
        private List<String> planCodes;
    }
}