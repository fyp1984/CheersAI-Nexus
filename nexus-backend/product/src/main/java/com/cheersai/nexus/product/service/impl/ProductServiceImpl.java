package com.cheersai.nexus.product.service.impl;

import com.cheersai.nexus.product.dto.ProductCreateDTO;
import com.cheersai.nexus.product.dto.ProductDetailDTO;
import com.cheersai.nexus.product.dto.ProductFeatureDTO;
import com.cheersai.nexus.product.dto.ProductFeatureUpdateDTO;
import com.cheersai.nexus.product.dto.ProductListQueryDTO;
import com.cheersai.nexus.product.dto.ProductListResponseDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogPageDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogQueryDTO;
import com.cheersai.nexus.product.dto.ProductUpdateDTO;
import com.cheersai.nexus.product.entity.Product;
import com.cheersai.nexus.product.entity.ProductOperationLog;
import com.cheersai.nexus.product.exception.ProductBusinessException;
import com.cheersai.nexus.product.exception.ProductErrorCode;
import com.cheersai.nexus.product.mapper.ProductMapper;
import com.cheersai.nexus.product.service.ProductOperationLogService;
import com.cheersai.nexus.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cheersai.nexus.product.entity.table.ProductTableDef.PRODUCT;

/**
 * 产品业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final Set<String> VALID_STATUS = Set.of("active", "inactive", "deprecated");

    private final ProductMapper productMapper;
    private final ProductOperationLogService productOperationLogService;
    private final ObjectMapper objectMapper;

    @Override
    public ProductListResponseDTO getProductList(ProductListQueryDTO queryDTO) {
        ProductListQueryDTO safeQuery = queryDTO != null ? queryDTO : new ProductListQueryDTO();
        int page = safePage(safeQuery.getPage());
        int pageSize = safePageSize(safeQuery.getPageSize());

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(PRODUCT)
                .where(PRODUCT.STATUS.eq(safeQuery.getStatus(), StringUtils.hasText(safeQuery.getStatus())))
                .and(PRODUCT.CURRENT_VERSION.eq(safeQuery.getCurrentVersion(), StringUtils.hasText(safeQuery.getCurrentVersion())))
                .and(PRODUCT.CREATED_AT.ge(safeQuery.getStartTime(), safeQuery.getStartTime() != null))
                .and(PRODUCT.CREATED_AT.le(safeQuery.getEndTime(), safeQuery.getEndTime() != null))
                .orderBy(PRODUCT.CREATED_AT, false);

        List<Product> allProducts = productMapper.selectListByQuery(queryWrapper);
        List<Product> keywordFiltered = filterByKeyword(allProducts, safeQuery.getKeyword());

        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= keywordFiltered.size()) {
            return ProductListResponseDTO.builder()
                    .items(Collections.emptyList())
                    .total((long) keywordFiltered.size())
                    .build();
        }
        int toIndex = Math.min(fromIndex + pageSize, keywordFiltered.size());
        List<ProductDetailDTO> items = keywordFiltered.subList(fromIndex, toIndex)
                .stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());

        return ProductListResponseDTO.builder()
                .items(items)
                .total((long) keywordFiltered.size())
                .build();
    }

    @Override
    public ProductDetailDTO getProductById(String id) {
        Product product = productMapper.selectOneById(parseUuid(id, "产品ID格式不正确"));
        if (product == null) {
            return null;
        }
        return toDetailDTO(product);
    }

    @Override
    public ProductDetailDTO getProductByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        Product product = productMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(PRODUCT)
                        .where(PRODUCT.CODE.eq(code))
        );
        if (product == null) {
            return null;
        }
        return toDetailDTO(product);
    }

    @Override
    @Transactional
    public void createProduct(ProductCreateDTO dto, String operatorId, String operatorName, String ipAddress) {
        validateCreateDTO(dto);
        ensureCodeUnique(dto.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .name(dto.getName().trim())
                .code(dto.getCode().trim())
                .description(dto.getDescription())
                .iconUrl(dto.getIconUrl())
                .status(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "active")
                .currentVersion(dto.getCurrentVersion())
                .downloadUrls(dto.getDownloadUrls())
                .settings(dto.getSettings())
                .createdAt(now)
                .updatedAt(now)
                .build();

        productMapper.insert(product);

        recordOperation(
                product,
                "创建产品",
                "product",
                product.getId(),
                buildContent("创建产品", product, operatorName),
                null,
                product,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void updateProduct(String id, ProductUpdateDTO dto, String operatorId, String operatorName, String ipAddress) {
        Product product = requireProduct(id);
        String beforeData = toJsonQuietly(product);

        if (StringUtils.hasText(dto.getCode()) && !dto.getCode().equalsIgnoreCase(product.getCode())) {
            ensureCodeUnique(dto.getCode(), id);
            product.setCode(dto.getCode().trim());
        }

        if (StringUtils.hasText(dto.getName())) {
            product.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getIconUrl() != null) {
            product.setIconUrl(dto.getIconUrl());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            ensureValidStatus(dto.getStatus());
            product.setStatus(dto.getStatus());
        }
        if (dto.getCurrentVersion() != null) {
            product.setCurrentVersion(dto.getCurrentVersion());
        }
        if (dto.getDownloadUrls() != null) {
            product.setDownloadUrls(dto.getDownloadUrls());
        }
        if (dto.getSettings() != null) {
            product.setSettings(dto.getSettings());
        }

        product.setUpdatedAt(LocalDateTime.now());
        productMapper.update(product);

        recordOperation(
                product,
                "编辑产品",
                "product",
                product.getId(),
                buildContent("编辑产品", product, operatorName),
                beforeData,
                product,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void deleteProduct(String id, String operatorId, String operatorName, String ipAddress) {
        Product product = requireProduct(id);
        String beforeData = toJsonQuietly(product);

        productMapper.deleteById(product.getId());

        recordOperation(
                product,
                "删除产品",
                "product",
                id,
                buildContent("删除产品", product, operatorName),
                beforeData,
                null,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void batchDeleteProducts(Iterable<String> ids, String operatorId, String operatorName, String ipAddress) {
        if (ids == null) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "待删除产品不能为空");
        }
        Set<String> deduplicatedIds = new LinkedHashSet<>();
        ids.forEach(deduplicatedIds::add);
        deduplicatedIds.removeIf(id -> !StringUtils.hasText(id));
        if (deduplicatedIds.isEmpty()) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "待删除产品不能为空");
        }

        for (String id : deduplicatedIds) {
            Product product = requireProduct(id);
            String beforeData = toJsonQuietly(product);
            productMapper.deleteById(product.getId());
            recordOperation(
                    product,
                    "批量删除产品",
                    "product",
                    id,
                    buildContent("批量删除产品", product, operatorName),
                    beforeData,
                    null,
                    operatorId,
                    operatorName,
                    ipAddress
            );
        }
    }

    @Override
    @Transactional
    public void updateProductStatus(String id, String status, String operatorId, String operatorName, String ipAddress) {
        Product product = requireProduct(id);
        ensureValidStatus(status);
        String beforeData = toJsonQuietly(product);

        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.update(product);

        recordOperation(
                product,
                "变更产品状态",
                "product",
                id,
                buildContent("变更产品状态", product, operatorName),
                beforeData,
                product,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    public ProductFeatureUpdateDTO getFeatures(String productId) {
        Product product = requireProduct(productId);
        ObjectNode settingsNode = parseSettingsObject(product.getSettings());
        List<ProductFeatureDTO> features;
        if (settingsNode.has("features")) {
            features = objectMapper.convertValue(settingsNode.get("features"), new TypeReference<List<ProductFeatureDTO>>() {
            });
        } else {
            features = new ArrayList<>();
        }
        return ProductFeatureUpdateDTO.builder()
                .features(features)
                .build();
    }

    @Override
    @Transactional
    public void updateFeatures(String productId, ProductFeatureUpdateDTO dto, String operatorId, String operatorName, String ipAddress) {
        Product product = requireProduct(productId);
        String beforeData = product.getSettings();
        ObjectNode settingsNode = parseSettingsObject(product.getSettings());
        List<ProductFeatureDTO> features = dto != null && dto.getFeatures() != null ? dto.getFeatures() : Collections.emptyList();
        settingsNode.set("features", objectMapper.valueToTree(features));

        try {
            product.setSettings(objectMapper.writeValueAsString(settingsNode));
        } catch (JsonProcessingException e) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "功能配置格式错误");
        }
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.update(product);

        recordOperation(
                product,
                "保存功能配置",
                "features",
                productId,
                buildContent("保存功能配置", product, operatorName),
                beforeData,
                product.getSettings(),
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    public ProductOperationLogPageDTO queryOperationLogs(ProductOperationLogQueryDTO queryDTO) {
        return productOperationLogService.queryLogs(queryDTO);
    }

    private void validateCreateDTO(ProductCreateDTO dto) {
        if (dto == null) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "请求体不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "产品名称不能为空");
        }
        if (!StringUtils.hasText(dto.getCode())) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "产品编码不能为空");
        }
        if (StringUtils.hasText(dto.getStatus())) {
            ensureValidStatus(dto.getStatus());
        }
    }

    private void ensureCodeUnique(String code, String excludeId) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);
        List<Product> products = productMapper.selectListByQuery(
                QueryWrapper.create()
                        .select(PRODUCT.ID, PRODUCT.CODE)
                        .from(PRODUCT)
        );
        boolean exists = products.stream()
                .anyMatch(item -> StringUtils.hasText(item.getCode())
                        && item.getCode().trim().toLowerCase(Locale.ROOT).equals(normalizedCode)
                        && (excludeId == null || !isSameUuid(excludeId, item.getId())));
        if (exists) {
            throw new ProductBusinessException(ProductErrorCode.PRODUCT_CODE_EXISTS, "产品编码已存在");
        }
    }

    private void ensureValidStatus(String status) {
        if (!StringUtils.hasText(status) || !VALID_STATUS.contains(status)) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_STATUS, "无效的产品状态");
        }
    }

    private Product requireProduct(String id) {
        Product product = productMapper.selectOneById(parseUuid(id, "产品ID格式不正确"));
        if (product == null) {
            throw new ProductBusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private List<Product> filterByKeyword(List<Product> products, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return products;
        }
        String needle = keyword.toLowerCase(Locale.ROOT).trim();
        return products.stream()
                .filter(product -> containsIgnoreCase(product.getName(), needle)
                        || containsIgnoreCase(product.getCode(), needle))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private int safePage(Integer page) {
        return page != null && page > 0 ? page : 1;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private ObjectNode parseSettingsObject(String settings) {
        if (!StringUtils.hasText(settings)) {
            return objectMapper.createObjectNode();
        }
        try {
            if (objectMapper.readTree(settings).isObject()) {
                return (ObjectNode) objectMapper.readTree(settings);
            }
        } catch (JsonProcessingException ignored) {
        }
        return objectMapper.createObjectNode();
    }

    private void recordOperation(Product product,
                                 String action,
                                 String targetType,
                                 String targetId,
                                 String content,
                                 Object beforeData,
                                 Object afterData,
                                 String operatorId,
                                 String operatorName,
                                 String ipAddress) {
        ProductOperationLog log = ProductOperationLog.builder()
                .productId(product != null ? product.getId() : null)
                .productCode(product != null ? product.getCode() : null)
                .productName(product != null ? product.getName() : null)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .content(content)
                .beforeData(toJsonQuietly(beforeData))
                .afterData(toJsonQuietly(afterData))
                .operatorId(normalizeOperatorId(operatorId))
                .operatorName(normalizeOperatorName(operatorName))
                .ipAddress(normalizeIp(ipAddress))
                .createdAt(LocalDateTime.now())
                .build();
        productOperationLogService.record(log);
    }

    private String buildContent(String action, Product product, String operatorName) {
        String safeProductName = product != null ? product.getName() : "-";
        String safeProductCode = product != null ? product.getCode() : "-";
        return String.format("%s | 产品:%s(%s) | 操作人:%s", action, safeProductName, safeProductCode, normalizeOperatorName(operatorName));
    }

    private String normalizeOperatorId(String operatorId) {
        return StringUtils.hasText(operatorId) ? operatorId : "system";
    }

    private String normalizeOperatorName(String operatorName) {
        return StringUtils.hasText(operatorName) ? operatorName : "系统";
    }

    private String normalizeIp(String ipAddress) {
        return StringUtils.hasText(ipAddress) ? ipAddress : "unknown";
    }

    private String parseUuid(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, message);
        }
        String normalized = normalizeUuid(value);
        try {
            return UUID.fromString(normalized).toString();
        } catch (Exception ex) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, message);
        }
    }

    private String normalizeUuid(String value) {
        String text = value.trim();
        if (text.length() == 32) {
            return text.substring(0, 8) + "-" + text.substring(8, 12) + "-" + text.substring(12, 16) + "-"
                    + text.substring(16, 20) + "-" + text.substring(20);
        }
        return text;
    }

    private boolean isSameUuid(String left, String right) {
        if (!StringUtils.hasText(left) || !StringUtils.hasText(right)) {
            return false;
        }
        return parseUuid(left, "UUID格式不正确").equals(parseUuid(right, "UUID格式不正确"));
    }

    private String toJsonQuietly(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private ProductDetailDTO toDetailDTO(Product product) {
        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .code(product.getCode())
                .description(product.getDescription())
                .iconUrl(product.getIconUrl())
                .status(product.getStatus())
                .currentVersion(product.getCurrentVersion())
                .downloadUrls(product.getDownloadUrls())
                .settings(product.getSettings())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
