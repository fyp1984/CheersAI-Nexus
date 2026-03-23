package com.cheersai.nexus.feedback.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class FeedbackTableDef extends TableDef {

    public static final FeedbackTableDef FEEDBACK = new FeedbackTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn TYPE = new QueryColumn(this, "type");

    public final QueryColumn TITLE = new QueryColumn(this, "title");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    public final QueryColumn PRIORITY = new QueryColumn(this, "priority");

    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    public final QueryColumn PRODUCT_ID = new QueryColumn(this, "product_id");

    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    public final QueryColumn ASSIGNEE_ID = new QueryColumn(this, "assignee_id");

    public final QueryColumn RESOLVED_AT = new QueryColumn(this, "resolved_at");

    public final QueryColumn ATTACHMENTS = new QueryColumn(this, "attachments");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TYPE, TITLE, STATUS, USER_ID, CONTENT, PRIORITY, CREATED_AT, PRODUCT_ID, UPDATED_AT, ASSIGNEE_ID, RESOLVED_AT, ATTACHMENTS};

    public FeedbackTableDef() {
        super("", "feedbacks");
    }

    private FeedbackTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public FeedbackTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new FeedbackTableDef("", "feedbacks", alias));
    }

}
