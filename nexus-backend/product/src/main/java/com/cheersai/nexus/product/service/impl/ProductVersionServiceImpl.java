package com.cheersai.nexus.product.service.impl;

import com.cheersai.nexus.product.dto.ProductVersionCreateDTO;
import com.cheersai.nexus.product.dto.ProductVersionDetailDTO;
import com.cheersai.nexus.product.entity.ProductVersion;
import com.cheersai.nexus.product.mapper.ProductMapper;
import com.cheersai.nexus.product.mapper.ProductVersionMapper;
import com.cheersai.nexus.product.service.ProductVersionService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


import static com.cheersai.nexus.product.entity.table.ProductVersionTableDef.PRODUCT_VERSION;

/**
 * 产品版本业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class ProductVersionServiceImpl extends ServiceImpl<ProductVersionMapper, ProductVersion> implements ProductVersionService {

    @Autowired
    private ProductVersionMapper productVersionMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductVersionDetailDTO> getVersionListByProductId(String productId) {
        List<ProductVersion> versions = productVersionMapper.selectListByQuery(
            QueryWrapper.create()
                    .select()
                    .from(PRODUCT_VERSION)
                .where(PRODUCT_VERSION.PRODUCT_ID.eq(productId))
                .orderBy(PRODUCT_VERSION.CREATED_AT, false)
        );
        
        return versions.stream()
            .map(this::toDetailDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductVersionDetailDTO getVersionById(String id) {
        ProductVersion version = productVersionMapper.selectOneById(id);
        if (version == null) {
            return null;
        }
        return toDetailDTO(version);
    }

    @Override
    @Transactional
    public void createVersion(String productId, ProductVersionCreateDTO dto, String userId, String userName) {
        // 检查产品是否存在
        var product = productMapper.selectOneById(productId);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        // 检查版本号是否已存在
        ProductVersion existing = productVersionMapper.selectOneByQuery(
            QueryWrapper.create()
                    .select()
                    .from(PRODUCT_VERSION)
                .where(PRODUCT_VERSION.PRODUCT_ID.eq(productId))
                .and(PRODUCT_VERSION.VERSION.eq(dto.getVersion()))
        );
        if (existing != null) {
            throw new RuntimeException("版本号已存在: " + dto.getVersion());
        }

        ProductVersion version = ProductVersion.builder()
            .productId(productId)
            .version(dto.getVersion())
            .versionName(dto.getVersionName())
            .status("draft")
            .changelog(dto.getChangelog())
            .downloadUrls(dto.getDownloadUrls())
            .releaseNote(dto.getReleaseNote())
            .forceUpdate(dto.getForceUpdate() != null ? dto.getForceUpdate() : false)
            .minVersion(dto.getMinVersion())
            .createdAt(LocalDateTime.now())
            .createdBy(userId)
            .createdByName(userName)
            .build();

        productVersionMapper.insert(version);
    }

    @Override
    @Transactional
    public void publishVersion(String id) {
        ProductVersion version = productVersionMapper.selectOneById(id);
        if (version == null) {
            throw new RuntimeException("版本不存在");
        }

        if ("published".equals(version.getStatus())) {
            throw new RuntimeException("版本已发布");
        }

        // 将该产品的其他版本设为非最新
        List<ProductVersion> otherVersions = productVersionMapper.selectListByQuery(
            QueryWrapper.create()
                    .select()
                    .from(PRODUCT_VERSION)
                .where(PRODUCT_VERSION.PRODUCT_ID.eq(version.getProductId()))
                .and(PRODUCT_VERSION.ID.ne(id))
                .and(PRODUCT_VERSION.STATUS.eq("published"))
        );
        
        for (ProductVersion other : otherVersions) {
            other.setStatus("deprecated");
            productVersionMapper.update(other);
        }

        // 发布当前版本
        version.setStatus("published");
        version.setPublishedAt(LocalDateTime.now());
        productVersionMapper.update(version);

        // 更新产品的当前版本
        var product = productMapper.selectOneById(version.getProductId());
        if (product != null) {
            product.setCurrentVersion(version.getVersion());
            productMapper.update(product);
        }
    }

    @Override
    @Transactional
    public void deprecateVersion(String id) {
        ProductVersion version = productVersionMapper.selectOneById(id);
        if (version == null) {
            throw new RuntimeException("版本不存在");
        }

        version.setStatus("deprecated");
        productVersionMapper.update(version);
    }

    @Override
    @Transactional
    public void deleteVersion(String id) {
        ProductVersion version = productVersionMapper.selectOneById(id);
        if (version == null) {
            throw new RuntimeException("版本不存在");
        }

        if ("published".equals(version.getStatus())) {
            throw new RuntimeException("不能删除已发布的版本");
        }

        productVersionMapper.deleteById(id);
    }

    @Override
    public ProductVersionDetailDTO getLatestVersion(String productId) {
        ProductVersion version = productVersionMapper.selectOneByQuery(
            QueryWrapper.create()
                    .select()
                    .from(PRODUCT_VERSION)
                .where(PRODUCT_VERSION.PRODUCT_ID.eq(productId))
                .and(PRODUCT_VERSION.STATUS.eq("published"))
                .orderBy(PRODUCT_VERSION.PUBLISHED_AT, false)
                .limit(1)
        );
        
        if (version == null) {
            return null;
        }
        return toDetailDTO(version);
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
