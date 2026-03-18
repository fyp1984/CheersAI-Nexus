package com.cheersai.nexus.common.model.base.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SysUserTableDef extends TableDef {

    /**
     * @ClassName:SysUser
 @Description:系统用户类（暂定）
 @Author:userSigma
 @CreateDate:2026/3/17 21:22
     */
    public static final SysUserTableDef SYS_USER = new SysUserTableDef();

    public final QueryColumn EMAIL = new QueryColumn(this, "email");

    public final QueryColumn PHONE = new QueryColumn(this, "phone");

    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    public final QueryColumn NICK_NAME = new QueryColumn(this, "nick_name");

    public final QueryColumn PASSWORD = new QueryColumn(this, "password");

    public final QueryColumn USER_NAME = new QueryColumn(this, "user_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{EMAIL, PHONE, USER_ID, NICK_NAME, PASSWORD, USER_NAME};

    public SysUserTableDef() {
        super("", "sys_user");
    }

    private SysUserTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysUserTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysUserTableDef("", "sys_user", alias));
    }

}
