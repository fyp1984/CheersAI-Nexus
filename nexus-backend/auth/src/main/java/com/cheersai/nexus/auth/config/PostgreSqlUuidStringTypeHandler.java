package com.cheersai.nexus.auth.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.context.annotation.Configuration;

import java.sql.*;

/**
 * PostgreSQL UUID 类型处理器（String <-> UUID）
 */
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.OTHER)
@Configuration
public class PostgreSqlUuidStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, normalizeUuid(parameter), Types.OTHER);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }

    private String normalizeUuid(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.length() == 32) {
            return text.substring(0, 8) + "-" + text.substring(8, 12) + "-" + text.substring(12, 16) + "-"
                    + text.substring(16, 20) + "-" + text.substring(20);
        }
        return text;
    }
}
