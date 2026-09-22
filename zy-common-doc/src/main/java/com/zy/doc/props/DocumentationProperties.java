package com.zy.doc.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * zy OpenAPI 文档配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "zy.documentation")
public class DocumentationProperties {

    /** 是否启用文档自动配置。 */
    private boolean enabled = true;

    /** 文档标题；为空时按应用名称推导。 */
    private String title;

    /** 分组标识。 */
    private String groupId = "default";

    /** 分组展示名称。 */
    private String groupDisplayName = "Default API";

    /** API 描述。 */
    private String description = "API documentation";

    /** API 版本。 */
    private String version = "1.0.0";
}
