package com.cheersai.nexus.feedback.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.UUID;

/**
 * PostgreSQL UUID 类型处理器
 */
@MappedTypes(UUID.class)  // 映射 Java 类型
@MappedJdbcTypes(JdbcType.OTHER)  // 映射 PostgreSQL 的 uuid 类型（JdbcType.OTHER）
public class PostgreSqlUuidTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter, Types.OTHER);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseUUID(rs.getString(columnName));
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseUUID(rs.getString(columnIndex));
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseUUID(cs.getString(columnIndex));
    }

    private UUID parseUUID(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}