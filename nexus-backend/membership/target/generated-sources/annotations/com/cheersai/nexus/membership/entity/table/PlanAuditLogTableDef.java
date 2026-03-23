package com.cheersai.nexus.membership.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class PlanAuditLogTableDef extends TableDef {

    /**
     * 会员计划审批/变更记录实体类
     */
    public static final PlanAuditLogTableDef PLAN_AUDIT_LOG = new PlanAuditLogTableDef();

    /**
     * 记录ID - UUID主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 关联的会员计划ID
     */
    public final QueryColumn PLAN_ID = new QueryColumn(this, "plan_id");

    /**
     * 变更后数据（JSON格式）
     */
    public final QueryColumn AFTER_DATA = new QueryColumn(this, "after_data");

    /**
     * 申请时间
     */
    public final QueryColumn APPLIED_AT = new QueryColumn(this, "applied_at");

    /**
     * 审批时间
     */
    public final QueryColumn AUDITED_AT = new QueryColumn(this, "audited_at");

    /**
     * 审批人ID
     */
    public final QueryColumn AUDITOR_ID = new QueryColumn(this, "auditor_id");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 变更前数据（JSON格式）
     */
    public final QueryColumn BEFORE_DATA = new QueryColumn(this, "before_data");

    /**
     * 申请人ID
     */
    public final QueryColumn APPLICANT_ID = new QueryColumn(this, "applicant_id");

    /**
     * 申请备注
     */
    public final QueryColumn APPLY_REMARK = new QueryColumn(this, "apply_remark");

    /**
     * 审批备注
     */
    public final QueryColumn AUDIT_REMARK = new QueryColumn(this, "audit_remark");

    /**
     * 审批状态：pending-待审批, approved-已通过, rejected-已驳回
     */
    public final QueryColumn AUDIT_STATUS = new QueryColumn(this, "audit_status");

    /**
     * 审批人名称
     */
    public final QueryColumn AUDITOR_NAME = new QueryColumn(this, "auditor_name");

    /**
     * 操作类型：create-创建, update-更新, delete-删除, status-状态变更, benefit-权益配置
     */
    public final QueryColumn OPERATE_TYPE = new QueryColumn(this, "operate_type");

    /**
     * 申请人名称
     */
    public final QueryColumn APPLICANT_NAME = new QueryColumn(this, "applicant_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PLAN_ID, AFTER_DATA, APPLIED_AT, AUDITED_AT, AUDITOR_ID, CREATED_AT, BEFORE_DATA, APPLICANT_ID, APPLY_REMARK, AUDIT_REMARK, AUDIT_STATUS, AUDITOR_NAME, OPERATE_TYPE, APPLICANT_NAME};

    public PlanAuditLogTableDef() {
        super("", "plan_audit_logs");
    }

    private PlanAuditLogTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public PlanAuditLogTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new PlanAuditLogTableDef("", "plan_audit_logs", alias));
    }

}
