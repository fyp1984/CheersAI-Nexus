package com.cheersai.nexus.user.model.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class RefreshTokenTableDef extends TableDef {

    /**
     * 刷新令牌实体
     */
    public static final RefreshTokenTableDef REFRESH_TOKEN = new RefreshTokenTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 刷新令牌
     */
    public final QueryColumn TOKEN = new QueryColumn(this, "token");

    /**
     * 用户ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 是否撤销
     */
    public final QueryColumn REVOKED = new QueryColumn(this, "revoked");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TOKEN, USER_ID, REVOKED, CREATED_AT, EXPIRES_AT};

    public RefreshTokenTableDef() {
        super("", "sys_refresh_token");
    }

    private RefreshTokenTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public RefreshTokenTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RefreshTokenTableDef("", "sys_refresh_token", alias));
    }

}
