package com.cheersai.nexus.product.service;

import com.cheersai.nexus.product.dto.ProductVersionCreateDTO;
import com.cheersai.nexus.product.dto.ProductVersionDetailDTO;
import com.cheersai.nexus.product.entity.ProductVersion;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 产品版本业务逻辑接口
 */
public interface ProductVersionService extends IService<ProductVersion> {

    /**
     * 获取产品的版本列表
     */
    List<ProductVersionDetailDTO> getVersionListByProductId(String productId);

    /**
     * 获取版本详情
     */
    ProductVersionDetailDTO getVersionById(String id);

    /**
     * 创建版本
     */
    void createVersion(String productId, ProductVersionCreateDTO dto, String userId, String userName);

    /**
     * 发布版本
     */
    void publishVersion(String id);

    /**
     * 废弃版本
     */
    void deprecateVersion(String id);

    /**
     * 删除版本
     */
    void deleteVersion(String id);

    /**
     * 获取产品的最新版本
     */
    ProductVersionDetailDTO getLatestVersion(String productId);
}
