package com.cheersai.nexus.product.service.impl;

import com.cheersai.nexus.product.dto.ProductOperationLogDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogPageDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogQueryDTO;
import com.cheersai.nexus.product.entity.ProductOperationLog;
import com.cheersai.nexus.product.mapper.ProductOperationLogMapper;
import com.cheersai.nexus.product.service.ProductOperationLogService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ProductOperationLogServiceImpl extends ServiceImpl<ProductOperationLogMapper, ProductOperationLog>
        implements ProductOperationLogService {

    @Override
    public void record(ProductOperationLog log) {
        this.save(log);
    }

    @Override
    public ProductOperationLogPageDTO queryLogs(ProductOperationLogQueryDTO queryDTO) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(ProductOperationLog.class)
                .where(ProductOperationLog::getCreatedAt).ge(queryDTO.getStartTime(), queryDTO.getStartTime() != null)
                .and(ProductOperationLog::getCreatedAt).le(queryDTO.getEndTime(), queryDTO.getEndTime() != null)
                .orderBy("created_at", false);

        List<ProductOperationLog> allLogs = this.list(queryWrapper);
        List<ProductOperationLog> keywordFiltered = applyKeywordFilter(allLogs, queryDTO.getKeyword());

        int page = queryDTO.getPage() != null && queryDTO.getPage() > 0 ? queryDTO.getPage() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= keywordFiltered.size()) {
            return ProductOperationLogPageDTO.builder()
                    .items(Collections.emptyList())
                    .total((long) keywordFiltered.size())
                    .build();
        }

        int toIndex = Math.min(fromIndex + pageSize, keywordFiltered.size());
        List<ProductOperationLogDTO> items = keywordFiltered.subList(fromIndex, toIndex)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ProductOperationLogPageDTO.builder()
                .items(items)
                .total((long) keywordFiltered.size())
                .build();
    }

    private List<ProductOperationLog> applyKeywordFilter(List<ProductOperationLog> logs, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return logs;
        }
        String needle = keyword.toLowerCase(Locale.ROOT).trim();
        return logs.stream()
                .filter(log -> containsIgnoreCase(log.getContent(), needle)
                        || containsIgnoreCase(log.getOperatorName(), needle)
                        || containsIgnoreCase(log.getAction(), needle)
                        || containsIgnoreCase(log.getProductName(), needle))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private ProductOperationLogDTO toDTO(ProductOperationLog log) {
        return ProductOperationLogDTO.builder()
                .id(log.getId())
                .productId(log.getProductId())
                .productCode(log.getProductCode())
                .productName(log.getProductName())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .content(log.getContent())
                .beforeData(log.getBeforeData())
                .afterData(log.getAfterData())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
