package com.cheersai.nexus.product.service.impl;

import com.cheersai.nexus.product.dto.ProductCreateDTO;
import com.cheersai.nexus.product.dto.ProductDetailDTO;
import com.cheersai.nexus.product.dto.ProductUpdateDTO;
import com.cheersai.nexus.product.entity.Product;
import com.cheersai.nexus.product.mapper.ProductMapper;
import com.cheersai.nexus.product.service.ProductService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.cheersai.nexus.product.entity.table.ProductTableDef.PRODUCT;

/**
 * 产品业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductDetailDTO> getProductList() {
        List<Product> products = productMapper.selectListByQuery(
            QueryWrapper.create()
                    .select("*")
                    .from(PRODUCT)
                    .orderBy(PRODUCT.CREATED_AT, false)
        );
        
        return products.stream()
            .map(this::toDetailDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductDetailDTO getProductById(String id) {
        Product product = productMapper.selectOneById(id);
        if (product == null) {
            return null;
        }
        return toDetailDTO(product);
    }

    @Override
    public ProductDetailDTO getProductByCode(String code) {
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
    public void createProduct(ProductCreateDTO dto) {
        // 检查编码是否已存在
        Product existing = productMapper.selectOneByQuery(
            QueryWrapper.create()
                    .select(PRODUCT.CODE)
                    .from(PRODUCT)
                .where(PRODUCT.CODE.eq(dto.getCode()))
        );
        if (existing != null) {
            throw new RuntimeException("产品编码已存在: " + dto.getCode());
        }

        Product product = Product.builder()
            .name(dto.getName())
            .code(dto.getCode())
            .description(dto.getDescription())
            .iconUrl(dto.getIconUrl())
            .status(dto.getStatus() != null ? dto.getStatus() : "active")
            .currentVersion(dto.getCurrentVersion())
            .downloadUrls(dto.getDownloadUrls())
            .settings(dto.getSettings())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        productMapper.insert(product);
    }

    @Override
    @Transactional
    public void updateProduct(String id, ProductUpdateDTO dto) {
        Product product = productMapper.selectOneById(id);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        // 如果更新了编码，检查是否与其他产品冲突
        if (dto.getCode() != null && !dto.getCode().equals(product.getCode())) {
            Product existing = productMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(PRODUCT)
                    .where(PRODUCT.CODE.eq(dto.getCode()))
            );
            if (existing != null) {
                throw new RuntimeException("产品编码已存在: " + dto.getCode());
            }
        }

        // 更新字段
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getIconUrl() != null) {
            product.setIconUrl(dto.getIconUrl());
        }
        if (dto.getStatus() != null) {
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
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        Product product = productMapper.selectOneById(id);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }
        productMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateProductStatus(String id, String status) {
        Product product = productMapper.selectOneById(id);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        if (!isValidStatus(status)) {
            throw new RuntimeException("无效的产品状态: " + status);
        }

        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.update(product);
    }

    private boolean isValidStatus(String status) {
        return "active".equals(status) || "inactive".equals(status) || "deprecated".equals(status);
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
