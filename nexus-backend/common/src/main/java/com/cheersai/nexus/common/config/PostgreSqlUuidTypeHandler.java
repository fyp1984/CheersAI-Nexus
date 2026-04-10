package com.cheersai.nexus.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.UUID;

/**
 * PostgreSQL UUID 类型处理器（java.util.UUID <-> UUID column）
 */
@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.OTHER)
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
