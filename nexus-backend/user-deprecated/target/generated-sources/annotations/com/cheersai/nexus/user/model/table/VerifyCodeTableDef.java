package com.cheersai.nexus.user.model.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class VerifyCodeTableDef extends TableDef {

    /**
     * 验证码实体
     */
    public static final VerifyCodeTableDef VERIFY_CODE = new VerifyCodeTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 验证码
     */
    public final QueryColumn CODE = new QueryColumn(this, "code");

    /**
     * 类型(email/phone)
     */
    public final QueryColumn TYPE = new QueryColumn(this, "type");

    /**
     * 验证码目标(邮箱/手机号)
     */
    public final QueryColumn TARGET = new QueryColumn(this, "target");

    /**
     * 用途(register/reset_password/login)
     */
    public final QueryColumn PURPOSE = new QueryColumn(this, "purpose");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 过期时间
     */
    public final QueryColumn EXPIRES_AT = new QueryColumn(this, "expires_at");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CODE, TYPE, TARGET, PURPOSE, CREATED_AT, EXPIRES_AT};

    public VerifyCodeTableDef() {
        super("", "sys_verify_code");
    }

    private VerifyCodeTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public VerifyCodeTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new VerifyCodeTableDef("", "sys_verify_code", alias));
    }

}
