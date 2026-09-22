package com.zy.doc.autoconfigure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.zy.doc.props.DocumentationProperties;

/**
 * zy Springdoc/OpenAPI 文档自动配置。
 */
@AutoConfiguration
@ConditionalOnClass({OpenAPI.class, GroupedOpenApi.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "zy.documentation", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DocumentationProperties.class)
public class DocumentationAutoConfiguration {

    /**
     * 注册默认 API 分组；同名 Bean 可由应用覆盖而不影响其他分组。
     */
    @Bean(name = "zyDocumentationGroupedOpenApi")
    @ConditionalOnMissingBean(name = "zyDocumentationGroupedOpenApi")
    public GroupedOpenApi zyDocumentationGroupedOpenApi(DocumentationProperties properties) {
        return GroupedOpenApi.builder()
                .group(resolveGroupId(properties))
                .displayName(resolveGroupDisplayName(properties))
                .pathsToMatch("/**")
                .build();
    }

    /**
     * 注册带 JWT bearer 认证方案的 OpenAPI 元数据，不声明全局认证要求。
     */
    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openAPI(DocumentationProperties properties, Environment environment) {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info()
                        .title(resolveTitle(properties, environment))
                        .description(properties.getDescription())
                        .version(properties.getVersion()))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
    }

    private String resolveTitle(DocumentationProperties properties, Environment environment) {
        if (StringUtils.hasText(properties.getTitle())) {
            return properties.getTitle();
        }
        String applicationName = environment.getProperty("spring.application.name");
        return StringUtils.hasText(applicationName) ? applicationName + " API" : "Application API";
    }

    private String resolveGroupId(DocumentationProperties properties) {
        return StringUtils.hasText(properties.getGroupId()) ? properties.getGroupId() : "default";
    }

    private String resolveGroupDisplayName(DocumentationProperties properties) {
        return StringUtils.hasText(properties.getGroupDisplayName())
                ? properties.getGroupDisplayName() : "Default API";
    }
}
