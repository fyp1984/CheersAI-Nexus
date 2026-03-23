package com.cheersai.nexus.product.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class ProductVersionTableDef extends TableDef {

    /**
     * 产品版本实体类
     */
    public static final ProductVersionTableDef PRODUCT_VERSION = new ProductVersionTableDef();

    /**
     * 版本ID - UUID主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 版本状态：draft-草稿, published-已发布, deprecated-已废弃
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 版本号（如 1.0.0, 1.0.1）
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 更新日志 - Markdown格式
     */
    public final QueryColumn CHANGELOG = new QueryColumn(this, "changelog");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 创建人ID
     */
    public final QueryColumn CREATED_BY = new QueryColumn(this, "created_by");

    /**
     * 关联的产品ID
     */
    public final QueryColumn PRODUCT_ID = new QueryColumn(this, "product_id");

    /**
     * 最低版本要求（用于增量更新）
     */
    public final QueryColumn MIN_VERSION = new QueryColumn(this, "min_version");

    /**
     * 是否强制更新
     */
    public final QueryColumn FORCE_UPDATE = new QueryColumn(this, "force_update");

    /**
     * 发布时间
     */
    public final QueryColumn PUBLISHED_AT = new QueryColumn(this, "published_at");

    /**
     * 发布说明
     */
    public final QueryColumn RELEASE_NOTE = new QueryColumn(this, "release_note");

    /**
     * 版本名称（如 正式版Beta版）
     */
    public final QueryColumn VERSION_NAME = new QueryColumn(this, "version_name");

    /**
     * 下载地址 - JSONB格式
     */
    public final QueryColumn DOWNLOAD_URLS = new QueryColumn(this, "download_urls");

    /**
     * 创建人名称
     */
    public final QueryColumn CREATED_BY_NAME = new QueryColumn(this, "created_by_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STATUS, VERSION, CHANGELOG, CREATED_AT, CREATED_BY, PRODUCT_ID, MIN_VERSION, FORCE_UPDATE, PUBLISHED_AT, RELEASE_NOTE, VERSION_NAME, DOWNLOAD_URLS, CREATED_BY_NAME};

    public ProductVersionTableDef() {
        super("", "product_versions");
    }

    private ProductVersionTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ProductVersionTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ProductVersionTableDef("", "product_versions", alias));
    }

}
