package com.cheersai.nexus.product.service;

import com.cheersai.nexus.product.dto.ProductCreateDTO;
import com.cheersai.nexus.product.dto.ProductDetailDTO;
import com.cheersai.nexus.product.dto.ProductFeatureUpdateDTO;
import com.cheersai.nexus.product.dto.ProductListQueryDTO;
import com.cheersai.nexus.product.dto.ProductListResponseDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogPageDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogQueryDTO;
import com.cheersai.nexus.product.dto.ProductUpdateDTO;
import com.cheersai.nexus.product.entity.Product;
import com.mybatisflex.core.service.IService;

/**
 * 产品业务逻辑接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 获取产品列表
     */
    ProductListResponseDTO getProductList(ProductListQueryDTO queryDTO);

    /**
     * 获取产品详情
     */
    ProductDetailDTO getProductById(String id);

    /**
     * 根据编码获取产品
     */
    ProductDetailDTO getProductByCode(String code);

    /**
     * 创建产品
     */
    void createProduct(ProductCreateDTO dto, String operatorId, String operatorName, String ipAddress);

    /**
     * 更新产品
     */
    void updateProduct(String id, ProductUpdateDTO dto, String operatorId, String operatorName, String ipAddress);

    /**
     * 删除产品
     */
    void deleteProduct(String id, String operatorId, String operatorName, String ipAddress);

    /**
     * 批量删除产品
     */
    void batchDeleteProducts(Iterable<String> ids, String operatorId, String operatorName, String ipAddress);

    /**
     * 更新产品状态
     */
    void updateProductStatus(String id, String status, String operatorId, String operatorName, String ipAddress);

    /**
     * 获取功能配置
     */
    ProductFeatureUpdateDTO getFeatures(String productId);

    /**
     * 保存功能配置
     */
    void updateFeatures(String productId, ProductFeatureUpdateDTO dto, String operatorId, String operatorName, String ipAddress);

    /**
     * 查询产品操作日志
     */
    ProductOperationLogPageDTO queryOperationLogs(ProductOperationLogQueryDTO queryDTO);
}
