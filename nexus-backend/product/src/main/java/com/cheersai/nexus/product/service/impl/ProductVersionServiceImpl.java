package com.cheersai.nexus.product.service.impl;

import com.cheersai.nexus.product.dto.ProductVersionCreateDTO;
import com.cheersai.nexus.product.dto.ProductVersionDetailDTO;
import com.cheersai.nexus.product.dto.ProductVersionUpdateDTO;
import com.cheersai.nexus.product.entity.Product;
import com.cheersai.nexus.product.entity.ProductOperationLog;
import com.cheersai.nexus.product.entity.ProductVersion;
import com.cheersai.nexus.product.exception.ProductBusinessException;
import com.cheersai.nexus.product.exception.ProductErrorCode;
import com.cheersai.nexus.product.mapper.ProductMapper;
import com.cheersai.nexus.product.mapper.ProductVersionMapper;
import com.cheersai.nexus.product.service.ProductOperationLogService;
import com.cheersai.nexus.product.service.ProductVersionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cheersai.nexus.product.entity.table.ProductVersionTableDef.PRODUCT_VERSION;

/**
 * 产品版本业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class ProductVersionServiceImpl extends ServiceImpl<ProductVersionMapper, ProductVersion> implements ProductVersionService {

    private final ProductVersionMapper productVersionMapper;
    private final ProductMapper productMapper;
    private final ProductOperationLogService productOperationLogService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProductVersionDetailDTO> getVersionListByProductId(String productId) {
        String productUuid = parseUuid(productId, "产品ID格式不正确");
        List<ProductVersion> versions = productVersionMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(PRODUCT_VERSION)
                        .where(PRODUCT_VERSION.PRODUCT_ID.eq(productUuid))
                        .orderBy(PRODUCT_VERSION.CREATED_AT, false)
        );

        return versions.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVersionDetailDTO getVersionById(String id) {
        ProductVersion version = productVersionMapper.selectOneByQuery(QueryWrapper.create().where(PRODUCT_VERSION.ID.eq(parseUuid(id, "版本ID格式不正确"))));
        if (version == null) {
            return null;
        }
        return toDetailDTO(version);
    }

    @Override
    @Transactional
    public void createVersion(String productId, ProductVersionCreateDTO dto, String userId, String userName, String ipAddress) {
        Product product = requireProduct(productId);
        validateVersionCreateDTO(dto);
        ensureVersionUnique(productId, dto.getVersion(), null);

        ProductVersion version = ProductVersion.builder()
                .productId(product.getId())
                .version(dto.getVersion().trim())
                .versionName(dto.getVersionName())
                .status("draft")
                .changelog(dto.getChangelog())
                .downloadUrls(dto.getDownloadUrls())
                .releaseNote(dto.getReleaseNote())
                .forceUpdate(dto.getForceUpdate() != null ? dto.getForceUpdate() : false)
                .minVersion(dto.getMinVersion())
                .createdAt(LocalDateTime.now())
                .createdBy(normalizeOperatorId(userId))
                .createdByName(normalizeOperatorName(userName))
                .build();

        productVersionMapper.insert(version);
        recordOperation(
                product,
                "创建版本",
                "version",
                version.getId(),
                "创建版本 " + version.getVersion(),
                null,
                version,
                userId,
                userName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void publishVersion(String productId, String id, String operatorId, String operatorName, String ipAddress) {
        String productUuid = parseUuid(productId, "产品ID格式不正确");
        String versionUuid = parseUuid(id, "版本ID格式不正确");
        Product product = requireProduct(productId);
        ProductVersion version = requireVersion(id);
        ensureVersionBelongsToProduct(productId, version);
        if ("published".equals(version.getStatus())) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "版本已发布");
        }
        String beforeData = toJsonQuietly(version);

        List<ProductVersion> otherPublishedVersions = productVersionMapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(PRODUCT_VERSION)
                        .where(PRODUCT_VERSION.PRODUCT_ID.eq(productUuid))
                        .and(PRODUCT_VERSION.ID.ne(versionUuid))
                        .and(PRODUCT_VERSION.STATUS.eq("published"))
        );
        for (ProductVersion other : otherPublishedVersions) {
            other.setStatus("deprecated");
            productVersionMapper.update(other);
        }

        version.setStatus("published");
        version.setPublishedAt(LocalDateTime.now());
        productVersionMapper.updateByQuery(version, QueryWrapper.create().where(PRODUCT_VERSION.ID.eq(version.getId())));

        product.setCurrentVersion(version.getVersion());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.update(product);

        recordOperation(
                product,
                "发布版本",
                "version",
                versionUuid,
                "发布版本 " + version.getVersion(),
                beforeData,
                version,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void deprecateVersion(String productId, String id, String operatorId, String operatorName, String ipAddress) {
        String versionUuid = parseUuid(id, "版本ID格式不正确");
        Product product = requireProduct(productId);
        ProductVersion version = requireVersion(id);
        ensureVersionBelongsToProduct(productId, version);
        String beforeData = toJsonQuietly(version);

        version.setStatus("deprecated");
        productVersionMapper.updateByQuery(version, QueryWrapper.create().where(PRODUCT_VERSION.ID.eq(version.getId())));

        if (StringUtils.hasText(product.getCurrentVersion()) && product.getCurrentVersion().equals(version.getVersion())) {
            product.setCurrentVersion(null);
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.update(product);
        }

        recordOperation(
                product,
                "废弃版本",
                "version",
                versionUuid,
                "废弃版本 " + version.getVersion(),
                beforeData,
                version,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void deleteVersion(String productId, String id, String operatorId, String operatorName, String ipAddress) {
        String versionUuid = parseUuid(id, "版本ID格式不正确");
        Product product = requireProduct(productId);
        ProductVersion version = requireVersion(id);
        ensureVersionBelongsToProduct(productId, version);

        if ("published".equals(version.getStatus())) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "不能删除已发布的版本");
        }

        String beforeData = toJsonQuietly(version);
        productVersionMapper.deleteByQuery(QueryWrapper.create().where(PRODUCT_VERSION.ID.eq(versionUuid)));

        recordOperation(
                product,
                "删除版本",
                "version",
                versionUuid,
                "删除版本 " + version.getVersion(),
                beforeData,
                null,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    @Transactional
    public void updateVersion(String productId, String versionId, ProductVersionUpdateDTO dto,
                              String operatorId, String operatorName, String ipAddress) {
        Product product = requireProduct(productId);
        ProductVersion version = requireVersion(versionId);
        ensureVersionBelongsToProduct(productId, version);
        if (dto == null) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "请求体不能为空");
        }

        String beforeData = toJsonQuietly(version);

        if (StringUtils.hasText(dto.getVersion()) && !dto.getVersion().equals(version.getVersion())) {
            ensureVersionUnique(productId, dto.getVersion(), versionId);
            version.setVersion(dto.getVersion().trim());
        }
        if (dto.getVersionName() != null) {
            version.setVersionName(dto.getVersionName());
        }
        if (dto.getChangelog() != null) {
            version.setChangelog(dto.getChangelog());
        }
        if (dto.getDownloadUrls() != null) {
            version.setDownloadUrls(dto.getDownloadUrls());
        }
        if (dto.getReleaseNote() != null) {
            version.setReleaseNote(dto.getReleaseNote());
        }
        if (dto.getForceUpdate() != null) {
            version.setForceUpdate(dto.getForceUpdate());
        }
        if (dto.getMinVersion() != null) {
            version.setMinVersion(dto.getMinVersion());
        }

        productVersionMapper.updateByQuery(version, QueryWrapper.create().where(PRODUCT_VERSION.ID.eq(version.getId())));

        if ("published".equals(version.getStatus())) {
            product.setCurrentVersion(version.getVersion());
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.update(product);
        }

        recordOperation(
                product,
                "编辑版本",
                "version",
                version.getId(),
                "编辑版本 " + version.getVersion(),
                beforeData,
                version,
                operatorId,
                operatorName,
                ipAddress
        );
    }

    @Override
    public ProductVersionDetailDTO getLatestVersion(String productId) {
        String productUuid = parseUuid(productId, "产品ID格式不正确");
        ProductVersion version = productVersionMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(PRODUCT_VERSION)
                        .where(PRODUCT_VERSION.PRODUCT_ID.eq(productUuid))
                        .and(PRODUCT_VERSION.STATUS.eq("published"))
                        .orderBy(PRODUCT_VERSION.PUBLISHED_AT, false)
                        .limit(1)
        );

        if (version == null) {
            return null;
        }
        return toDetailDTO(version);
    }

    private void validateVersionCreateDTO(ProductVersionCreateDTO dto) {
        if (dto == null) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "请求体不能为空");
        }
        if (!StringUtils.hasText(dto.getVersion())) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "版本号不能为空");
        }
    }

    private void ensureVersionUnique(String productId, String versionCode, String excludeVersionId) {
        String productUuid = parseUuid(productId, "产品ID格式不正确");
        List<ProductVersion> versions = productVersionMapper.selectListByQuery(
                QueryWrapper.create()
                        .select(PRODUCT_VERSION.ID, PRODUCT_VERSION.VERSION)
                        .from(PRODUCT_VERSION)
                        .where(PRODUCT_VERSION.PRODUCT_ID.eq(productUuid))
        );
        boolean exists = versions.stream()
                .anyMatch(item -> StringUtils.hasText(item.getVersion())
                        && item.getVersion().trim().equalsIgnoreCase(versionCode.trim())
                        && (excludeVersionId == null || !isSameUuid(excludeVersionId, item.getId())));
        if (exists) {
            throw new ProductBusinessException(ProductErrorCode.VERSION_EXISTS, "版本号已存在");
        }
    }

    private Product requireProduct(String productId) {
        Product product = productMapper.selectOneByQuery(QueryWrapper.create().where(PRODUCT.ID.eq(parseUuid(productId, "产品ID格式不正确"))));
        if (product == null) {
            throw new ProductBusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private ProductVersion requireVersion(String versionId) {
        ProductVersion version = productVersionMapper.selectOneByQuery(QueryWrapper.create().where(PRODUCT_VERSION.ID.eq(parseUuid(versionId, "版本ID格式不正确"))));
        if (version == null) {
            throw new ProductBusinessException(ProductErrorCode.VERSION_NOT_FOUND);
        }
        return version;
    }

    private void ensureVersionBelongsToProduct(String productId, ProductVersion version) {
        String productUuid = parseUuid(productId, "产品ID格式不正确");
        String versionProductId = parseUuid(version.getProductId(), "版本与产品不匹配");
        if (!productUuid.equals(versionProductId)) {
            throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "版本与产品不匹配");
        }
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
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(product.getName())
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .content(content)
                .beforeData(toJsonQuietly(beforeData))
                .afterData(toJsonQuietly(afterData))
                .operatorId(normalizeOperatorId(operatorId))
                .operatorName(normalizeOperatorName(operatorName))
                .ipAddress(StringUtils.hasText(ipAddress) ? ipAddress : "unknown")
                .createdAt(LocalDateTime.now())
                .build();
        productOperationLogService.record(log);
    }

    private String normalizeOperatorId(String operatorId) {
        return StringUtils.hasText(operatorId) ? operatorId : "system";
    }

    private String normalizeOperatorName(String operatorName) {
        return StringUtils.hasText(operatorName) ? operatorName : "系统";
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

    private ProductVersionDetailDTO toDetailDTO(ProductVersion version) {
        return ProductVersionDetailDTO.builder()
                .id(version.getId())
                .productId(version.getProductId())
                .version(version.getVersion())
                .versionName(version.getVersionName())
                .status(version.getStatus())
                .changelog(version.getChangelog())
                .downloadUrls(version.getDownloadUrls())
                .releaseNote(version.getReleaseNote())
                .forceUpdate(version.getForceUpdate())
                .minVersion(version.getMinVersion())
                .publishedAt(version.getPublishedAt())
                .createdAt(version.getCreatedAt())
                .createdBy(version.getCreatedBy())
                .createdByName(version.getCreatedByName())
                .build();
    }
}
