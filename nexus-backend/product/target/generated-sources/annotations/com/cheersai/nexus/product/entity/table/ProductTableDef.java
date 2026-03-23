package com.cheersai.nexus.product.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class ProductTableDef extends TableDef {

    /**
     * 产品实体类
     */
    public static final ProductTableDef PRODUCT = new ProductTableDef();

    /**
     * 产品ID - UUID主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 产品编码 - 唯一标识
     */
    public final QueryColumn CODE = new QueryColumn(this, "code");

    /**
     * 产品名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 产品状态：active-启用, inactive-停用, deprecated-已废弃
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 产品图标URL
     */
    public final QueryColumn ICON_URL = new QueryColumn(this, "icon_url");

    /**
     * 产品设置 - JSONB格式存储功能开关等配置
 结构: {"featureFlags": {"darkMode": true, "apiAccess": false}, "customConfig": {...}}
     */
    public final QueryColumn SETTINGS = new QueryColumn(this, "settings");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 产品描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 下载地址 - JSONB格式存储多个下载地址
 结构: [{"platform": "windows", "url": "https://...", "version": "1.0.0"}, ...]
     */
    public final QueryColumn DOWNLOAD_URLS = new QueryColumn(this, "download_urls");

    /**
     * 当前版本号
     */
    public final QueryColumn CURRENT_VERSION = new QueryColumn(this, "current_version");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CODE, NAME, STATUS, ICON_URL, SETTINGS, CREATED_AT, UPDATED_AT, DESCRIPTION, DOWNLOAD_URLS, CURRENT_VERSION};

    public ProductTableDef() {
        super("", "products");
    }

    private ProductTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ProductTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ProductTableDef("", "products", alias));
    }

}
