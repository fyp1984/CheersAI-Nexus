package com.cheersai.nexus.product.service;

import com.cheersai.nexus.product.dto.ProductCreateDTO;
import com.cheersai.nexus.product.dto.ProductDetailDTO;
import com.cheersai.nexus.product.dto.ProductUpdateDTO;
import com.cheersai.nexus.product.entity.Product;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 产品业务逻辑接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 获取产品列表
     */
    List<ProductDetailDTO> getProductList();

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
    void createProduct(ProductCreateDTO dto);

    /**
     * 更新产品
     */
    void updateProduct(String id, ProductUpdateDTO dto);

    /**
     * 删除产品
     */
    void deleteProduct(String id);

    /**
     * 更新产品状态
     */
    void updateProductStatus(String id, String status);
}
