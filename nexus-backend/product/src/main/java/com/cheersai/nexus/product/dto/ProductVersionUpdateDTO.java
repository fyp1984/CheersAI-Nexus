package com.cheersai.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品版本编辑 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersionUpdateDTO {

    private String version;
    private String versionName;
    private String changelog;
    private String downloadUrls;
    private String releaseNote;
    private Boolean forceUpdate;
    private String minVersion;
}
