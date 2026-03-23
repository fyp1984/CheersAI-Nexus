package com.cheersai.nexus.membership.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class MembershipPlanTableDef extends TableDef {

    /**
     * 会员计划实体类
     */
    public static final MembershipPlanTableDef MEMBERSHIP_PLAN = new MembershipPlanTableDef();

    /**
     * 计划ID - UUID主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 计划编码 - 唯一标识（如 free, pro, team, enterprise）
     */
    public final QueryColumn CODE = new QueryColumn(this, "code");

    /**
     * 计划名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 额度配置 - JSONB格式
     */
    public final QueryColumn LIMITS = new QueryColumn(this, "limits");

    /**
     * 状态：active-启用, disabled-禁用
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 货币类型
     */
    public final QueryColumn CURRENCY = new QueryColumn(this, "currency");

    /**
     * 权益配置 - JSONB格式
     */
    public final QueryColumn FEATURES = new QueryColumn(this, "features");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 计划描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 年付价格
     */
    public final QueryColumn PRICE_YEARLY = new QueryColumn(this, "price_yearly");

    /**
     * 月付价格
     */
    public final QueryColumn PRICE_MONTHLY = new QueryColumn(this, "price_monthly");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CODE, NAME, LIMITS, STATUS, CURRENCY, FEATURES, CREATED_AT, SORT_ORDER, UPDATED_AT, DESCRIPTION, PRICE_YEARLY, PRICE_MONTHLY};

    public MembershipPlanTableDef() {
        super("", "membership_plans");
    }

    private MembershipPlanTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public MembershipPlanTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new MembershipPlanTableDef("", "membership_plans", alias));
    }

}
