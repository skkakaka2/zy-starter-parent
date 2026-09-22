package com.zy.doc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文档自动配置的无 Web 服务器测试。
 */
class DocumentationAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DocumentationAutoConfiguration.class));

    @Test
    void resolvesTitleAndRegistersBearerSchemeAndDefaultGroup() {
        contextRunner.withPropertyValues("spring.application.name=order-service")
                .run(context -> {
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    assertThat(openAPI.getInfo().getTitle()).isEqualTo("order-service API");
                    assertThat(openAPI.getInfo().getDescription()).isEqualTo("API documentation");
                    assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
                    assertThat(openAPI.getSecurity()).isNull();

                    SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
                    assertThat(bearerAuth.getType()).isEqualTo(SecurityScheme.Type.HTTP);
                    assertThat(bearerAuth.getScheme()).isEqualTo("bearer");
                    assertThat(bearerAuth.getBearerFormat()).isEqualTo("JWT");

                    GroupedOpenApi group = context.getBean("zyDocumentationGroupedOpenApi", GroupedOpenApi.class);
                    assertThat(group.getGroup()).isEqualTo("default");
                    assertThat(group.getDisplayName()).isEqualTo("Default API");
                    assertThat(group.getPathsToMatch()).containsExactly("/**");
                });
    }

    @Test
    void explicitTitleTakesPrecedenceOverApplicationName() {
        contextRunner.withPropertyValues(
                "spring.application.name=order-service",
                "zy.documentation.title=Order Center API")
                .run(context -> assertThat(context.getBean(OpenAPI.class).getInfo().getTitle())
                        .isEqualTo("Order Center API"));
    }

    @Test
    void backsOffForExplicitApplicationOpenApiBean() {
        contextRunner.withUserConfiguration(UserOpenApiConfiguration.class)
                .run(context -> assertThat(context.getBean(OpenAPI.class))
                        .isSameAs(UserOpenApiConfiguration.OPEN_API));
    }

    @Test
    void backsOffWhenDocumentationIsDisabled() {
        contextRunner.withPropertyValues("zy.documentation.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OpenAPI.class);
                    assertThat(context).doesNotHaveBean("zyDocumentationGroupedOpenApi");
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserOpenApiConfiguration {

        private static final OpenAPI OPEN_API = new OpenAPI();

        @Bean
        OpenAPI userOpenApi() {
            return OPEN_API;
        }
    }
}
