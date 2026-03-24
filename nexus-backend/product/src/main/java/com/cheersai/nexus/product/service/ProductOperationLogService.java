package com.cheersai.nexus.product.service;

import com.cheersai.nexus.product.dto.ProductOperationLogPageDTO;
import com.cheersai.nexus.product.dto.ProductOperationLogQueryDTO;
import com.cheersai.nexus.product.entity.ProductOperationLog;
import com.mybatisflex.core.service.IService;

public interface ProductOperationLogService extends IService<ProductOperationLog> {

    void record(ProductOperationLog log);

    ProductOperationLogPageDTO queryLogs(ProductOperationLogQueryDTO queryDTO);
}
