package com.cheersai.nexus.membership.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SubscriptionTableDef extends TableDef {

    /**
     * 用户订阅实体类
     */
    public static final SubscriptionTableDef SUBSCRIPTION = new SubscriptionTableDef();

    /**
     * 订阅ID - UUID主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 订阅状态：active-生效中, expired-已过期, cancelled-已取消
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 用户ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 结束日期
     */
    public final QueryColumn END_DATE = new QueryColumn(this, "end_date");

    /**
     * 会员计划编码
     */
    public final QueryColumn PLAN_CODE = new QueryColumn(this, "plan_code");

    /**
     * 是否自动续费
     */
    public final QueryColumn AUTO_RENEW = new QueryColumn(this, "auto_renew");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 开始日期
     */
    public final QueryColumn START_DATE = new QueryColumn(this, "start_date");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 最后支付时间
     */
    public final QueryColumn LAST_PAYMENT_AT = new QueryColumn(this, "last_payment_at");

    /**
     * 支付方式
     */
    public final QueryColumn PAYMENT_METHOD = new QueryColumn(this, "payment_method");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STATUS, USER_ID, END_DATE, PLAN_CODE, AUTO_RENEW, CREATED_AT, START_DATE, UPDATED_AT, LAST_PAYMENT_AT, PAYMENT_METHOD};

    public SubscriptionTableDef() {
        super("", "subscriptions");
    }

    private SubscriptionTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SubscriptionTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SubscriptionTableDef("", "subscriptions", alias));
    }

}
