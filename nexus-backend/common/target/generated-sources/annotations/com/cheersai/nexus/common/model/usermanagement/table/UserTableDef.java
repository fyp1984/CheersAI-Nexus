package com.cheersai.nexus.common.model.usermanagement.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class UserTableDef extends TableDef {

    /**
     * 用户实体类
     */
    public static final UserTableDef USER = new UserTableDef();

    public final QueryColumn EMAIL = new QueryColumn(this, "email");

    public final QueryColumn PHONE = new QueryColumn(this, "phone");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    public final QueryColumn NICKNAME = new QueryColumn(this, "nickname");

    public final QueryColumn USERNAME = new QueryColumn(this, "username");

    public final QueryColumn AVATAR_URL = new QueryColumn(this, "avatar_url");

    public final QueryColumn CREATED_AT = new QueryColumn(this, "create_at");

    public final QueryColumn UPDATED_AT = new QueryColumn(this, "update_at");

    public final QueryColumn LAST_LOGIN_AT = new QueryColumn(this, "last_login_at");

    public final QueryColumn LAST_LOGIN_IP = new QueryColumn(this, "last_login_ip");

    public final QueryColumn PASSWORD_HASH = new QueryColumn(this, "password_hash");

    public final QueryColumn EMAIL_VERIFIED = new QueryColumn(this, "email_verified");

    public final QueryColumn PHONE_VERIFIED = new QueryColumn(this, "phone_verified");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{EMAIL, PHONE, STATUS, USER_ID, NICKNAME, USERNAME, AVATAR_URL, CREATED_AT, UPDATED_AT, LAST_LOGIN_AT, LAST_LOGIN_IP, PASSWORD_HASH, EMAIL_VERIFIED, PHONE_VERIFIED};

    public UserTableDef() {
        super("", "user");
    }

    private UserTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public UserTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new UserTableDef("", "user", alias));
    }

}
