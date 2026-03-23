package com.cheersai.nexus.product.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.product.dto.ProductCreateDTO;
import com.cheersai.nexus.product.dto.ProductDetailDTO;
import com.cheersai.nexus.product.dto.ProductUpdateDTO;
import com.cheersai.nexus.product.dto.ProductVersionCreateDTO;
import com.cheersai.nexus.product.dto.ProductVersionDetailDTO;
import com.cheersai.nexus.product.service.ProductService;
import com.cheersai.nexus.product.service.ProductVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品管理控制器
 */
@RestController("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductVersionService productVersionService;

    /**
     * 获取产品列表
     */
    @GetMapping
    public Result<List<ProductDetailDTO>> getProductList() {
        return Result.success(productService.getProductList());
    }

    /**
     * 获取产品详情
     */
    @GetMapping("/{id}")
    public Result<ProductDetailDTO> getProductDetail(@PathVariable String id) {
        ProductDetailDTO product = productService.getProductById(id);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }

    /**
     * 创建产品
     */
    @PostMapping
    public Result<Void> createProduct(@RequestBody ProductCreateDTO dto,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productService.createProduct(dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新产品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable String id,
                                      @RequestBody ProductUpdateDTO dto,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productService.updateProduct(id, dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable String id,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productService.deleteProduct(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新产品状态
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable String id,
                                            @RequestParam("status") String status,
                                            @RequestHeader(value = "X-User-Id", required = false) String userId,
                                            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productService.updateProductStatus(id, status);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取产品的版本列表
     */
    @GetMapping("/{id}/versions")
    public Result<List<ProductVersionDetailDTO>> getVersionList(@PathVariable String id) {
        return Result.success(productVersionService.getVersionListByProductId(id));
    }

    /**
     * 创建新版本（草稿）
     */
    @PostMapping("/{id}/versions")
    public Result<Void> createVersion(@PathVariable String id,
                                      @RequestBody ProductVersionCreateDTO dto,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String operatorId = userId != null ? userId : "system";
        String operatorName = userName != null ? userName : "系统";
        
        try {
            productVersionService.createVersion(id, dto, operatorId, operatorName);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布版本
     */
    @PostMapping("/{productId}/versions/{versionId}/publish")
    public Result<Void> publishVersion(@PathVariable String productId,
                                       @PathVariable String versionId,
                                       @RequestHeader(value = "X-User-Id", required = false) String userId,
                                       @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productVersionService.publishVersion(versionId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 废弃版本
     */
    @PostMapping("/{productId}/versions/{versionId}/deprecate")
    public Result<Void> deprecateVersion(@PathVariable String productId,
                                         @PathVariable String versionId,
                                         @RequestHeader(value = "X-User-Id", required = false) String userId,
                                         @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productVersionService.deprecateVersion(versionId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除版本
     */
    @DeleteMapping("/{productId}/versions/{versionId}")
    public Result<Void> deleteVersion(@PathVariable String productId,
                                      @PathVariable String versionId,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            productVersionService.deleteVersion(versionId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取最新版本
     */
    @GetMapping("/{id}/versions/latest")
    public Result<ProductVersionDetailDTO> getLatestVersion(@PathVariable String id) {
        ProductVersionDetailDTO version = productVersionService.getLatestVersion(id);
        if (version == null) {
            return Result.error("暂无已发布的版本");
        }
        return Result.success(version);
    }
}
