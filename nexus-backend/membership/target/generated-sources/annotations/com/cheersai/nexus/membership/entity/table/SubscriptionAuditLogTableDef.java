package com.cheersai.nexus.membership.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SubscriptionAuditLogTableDef extends TableDef {

    /**
     * 用户订阅变更审计日志实体类
     */
    public static final SubscriptionAuditLogTableDef SUBSCRIPTION_AUDIT_LOG = new SubscriptionAuditLogTableDef();

    /**
     * 记录ID - UUID主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 操作原因/备注
     */
    public final QueryColumn REASON = new QueryColumn(this, "reason");

    /**
     * 用户ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 操作人ID
     */
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    /**
     * 操作人IP
     */
    public final QueryColumn OPERATOR_IP = new QueryColumn(this, "operator_ip");

    /**
     * 操作类型：adjust-手动调整, upgrade-升级, downgrade-降级, extend-延长, shorten-缩短
     */
    public final QueryColumn OPERATE_TYPE = new QueryColumn(this, "operate_type");

    /**
     * 变更后到期日期
     */
    public final QueryColumn AFTER_END_DATE = new QueryColumn(this, "after_end_date");

    /**
     * 操作人名称
     */
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    /**
     * 变更后计划编码
     */
    public final QueryColumn AFTER_PLAN_CODE = new QueryColumn(this, "after_plan_code");

    /**
     * 变更前到期日期
     */
    public final QueryColumn BEFORE_END_DATE = new QueryColumn(this, "before_end_date");

    /**
     * 变更前计划编码
     */
    public final QueryColumn BEFORE_PLAN_CODE = new QueryColumn(this, "before_plan_code");

    /**
     * 订阅ID
     */
    public final QueryColumn SUBSCRIPTION_ID = new QueryColumn(this, "subscription_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REASON, USER_ID, CREATED_AT, OPERATOR_ID, OPERATOR_IP, OPERATE_TYPE, AFTER_END_DATE, OPERATOR_NAME, AFTER_PLAN_CODE, BEFORE_END_DATE, BEFORE_PLAN_CODE, SUBSCRIPTION_ID};

    public SubscriptionAuditLogTableDef() {
        super("", "subscription_audit_logs");
    }

    private SubscriptionAuditLogTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SubscriptionAuditLogTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SubscriptionAuditLogTableDef("", "subscription_audit_logs", alias));
    }

}
