package com.cheersai.nexus.product.controller;

import com.cheersai.nexus.common.model.base.Result;
import com.cheersai.nexus.product.dto.ProductBatchDeleteDTO;
import com.cheersai.nexus.product.dto.ProductCreateDTO;
import com.cheersai.nexus.product.dto.ProductDetailDTO;
import com.cheersai.nexus.product.dto.ProductFeatureUpdateDTO;
import com.cheersai.nexus.product.dto.ProductListQueryDTO;
import com.cheersai.nexus.product.dto.ProductListResponseDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogPageDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogQueryDTO;
import com.cheersai.nexus.product.dto.ProductUpdateDTO;
import com.cheersai.nexus.product.dto.ProductVersionCreateDTO;
import com.cheersai.nexus.product.dto.ProductVersionDetailDTO;
import com.cheersai.nexus.product.dto.ProductVersionUpdateDTO;
import com.cheersai.nexus.product.exception.ProductBusinessException;
import com.cheersai.nexus.product.exception.ProductErrorCode;
import com.cheersai.nexus.product.service.ProductService;
import com.cheersai.nexus.product.service.ProductVersionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * 产品管理控制器
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductVersionService productVersionService;

    /**
     * 获取产品列表
     */
    @GetMapping
    public Result<ProductListResponseDTO> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String currentVersion,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        ProductListQueryDTO queryDTO = ProductListQueryDTO.builder()
                .keyword(keyword)
                .status(status)
                .startTime(parseDateTime(startTime, false))
                .endTime(parseDateTime(endTime, true))
                .currentVersion(currentVersion)
                .page(page)
                .pageSize(pageSize)
                .build();
        return Result.success(productService.getProductList(queryDTO));
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
                                      @RequestHeader(value = "X-User-Name", required = false) String userName,
                                      HttpServletRequest request) {
        productService.createProduct(dto, normalizeOperatorId(userId), normalizeOperatorName(userName), getClientIp(request));
        return Result.success();
    }

    /**
     * 更新产品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable String id,
                                      @RequestBody @Valid ProductUpdateDTO dto,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName,
                                      HttpServletRequest request) {
        productService.updateProduct(id, dto, normalizeOperatorId(userId), normalizeOperatorName(userName), getClientIp(request));
        return Result.success();
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable String id,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName,
                                      HttpServletRequest request) {
        productService.deleteProduct(id, normalizeOperatorId(userId), normalizeOperatorName(userName), getClientIp(request));
        return Result.success();
    }

    /**
     * 批量删除产品
     */
    @PostMapping("/batch-delete")
    public Result<Void> batchDeleteProducts(@RequestBody ProductBatchDeleteDTO dto,
                                            @RequestHeader(value = "X-User-Id", required = false) String userId,
                                            @RequestHeader(value = "X-User-Name", required = false) String userName,
                                            HttpServletRequest request) {
        productService.batchDeleteProducts(dto != null ? dto.getIds() : null,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
    }

    /**
     * 更新产品状态
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable String id,
                                            @RequestParam("status") String status,
                                            @RequestHeader(value = "X-User-Id", required = false) String userId,
                                            @RequestHeader(value = "X-User-Name", required = false) String userName,
                                            HttpServletRequest request) {
        productService.updateProductStatus(id, status,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
    }

    /**
     * 获取功能配置
     */
    @GetMapping("/{id}/features")
    public Result<ProductFeatureUpdateDTO> getProductFeatures(@PathVariable String id) {
        return Result.success(productService.getFeatures(id));
    }

    /**
     * 保存功能配置
     */
    @PutMapping("/{id}/features")
    public Result<Void> updateProductFeatures(@PathVariable String id,
                                              @RequestBody ProductFeatureUpdateDTO dto,
                                              @RequestHeader(value = "X-User-Id", required = false) String userId,
                                              @RequestHeader(value = "X-User-Name", required = false) String userName,
                                              HttpServletRequest request) {
        productService.updateFeatures(id, dto, normalizeOperatorId(userId), normalizeOperatorName(userName), getClientIp(request));
        return Result.success();
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
                                      @RequestHeader(value = "X-User-Name", required = false) String userName,
                                      HttpServletRequest request) {
        productVersionService.createVersion(id, dto,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
    }

    /**
     * 编辑版本
     */
    @PutMapping("/{productId}/versions/{versionId}")
    public Result<Void> updateVersion(@PathVariable String productId,
                                      @PathVariable String versionId,
                                      @RequestBody ProductVersionUpdateDTO dto,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName,
                                      HttpServletRequest request) {
        productVersionService.updateVersion(productId, versionId, dto,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
    }

    /**
     * 发布版本
     */
    @PostMapping("/{productId}/versions/{versionId}/publish")
    public Result<Void> publishVersion(@PathVariable String productId,
                                       @PathVariable String versionId,
                                       @RequestHeader(value = "X-User-Id", required = false) String userId,
                                       @RequestHeader(value = "X-User-Name", required = false) String userName,
                                       HttpServletRequest request) {
        productVersionService.publishVersion(productId, versionId,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
    }

    /**
     * 废弃版本
     */
    @PostMapping("/{productId}/versions/{versionId}/deprecate")
    public Result<Void> deprecateVersion(@PathVariable String productId,
                                         @PathVariable String versionId,
                                         @RequestHeader(value = "X-User-Id", required = false) String userId,
                                         @RequestHeader(value = "X-User-Name", required = false) String userName,
                                         HttpServletRequest request) {
        productVersionService.deprecateVersion(productId, versionId,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
    }

    /**
     * 删除版本
     */
    @DeleteMapping("/{productId}/versions/{versionId}")
    public Result<Void> deleteVersion(@PathVariable String productId,
                                      @PathVariable String versionId,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @RequestHeader(value = "X-User-Name", required = false) String userName,
                                      HttpServletRequest request) {
        productVersionService.deleteVersion(productId, versionId,
                normalizeOperatorId(userId),
                normalizeOperatorName(userName),
                getClientIp(request));
        return Result.success();
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

    /**
     * 查询产品操作日志
     */
    @GetMapping("/logs")
    public Result<ProductOperationLogPageDTO> getOperationLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        ProductOperationLogQueryDTO queryDTO = ProductOperationLogQueryDTO.builder()
                .keyword(keyword)
                .startTime(parseDateTime(startTime, false))
                .endTime(parseDateTime(endTime, true))
                .page(page)
                .pageSize(pageSize)
                .build();
        return Result.success(productService.queryOperationLogs(queryDTO));
    }

    private String normalizeOperatorId(String userId) {
        return StringUtils.hasText(userId) ? userId : "system";
    }

    private String normalizeOperatorName(String userName) {
        return StringUtils.hasText(userName) ? userName : "系统";
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private LocalDateTime parseDateTime(String value, boolean isEndTime) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(text);
            return isEndTime ? date.atTime(LocalTime.MAX) : date.atStartOfDay();
        } catch (Exception ignored) {
        }
        throw new ProductBusinessException(ProductErrorCode.INVALID_PARAMETER, "时间格式不正确");
    }
}
