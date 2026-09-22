package com.zy.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.web.aspect.WebLogAspect;
import com.zy.web.exception.GlobalExceptionHandler;
import com.zy.web.props.WebProperties;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 自动装配验证：Bean 是否注册、开关是否生效、Jackson 约定是否接入。
 */
class WebAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebAutoConfiguration.class));

    @Test
    void shouldRegisterCoreBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
            assertThat(context).hasSingleBean(CorsFilter.class);
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);
            assertThat(context).hasSingleBean(WebLogAspect.class);
            assertThat(context).hasSingleBean(Validator.class);
            assertThat(context).hasSingleBean(WebProperties.class);
            assertThat(context).hasBean("traceIdFilter");
            assertThat(context.getBean(WebProperties.class).getCors().isAllowCredentials()).isFalse();
        });
    }

    @Test
    void shouldBackOffWhenSwitchedOff() {
        contextRunner.withPropertyValues("zy.web.cors.enabled=false", "zy.web.log.enabled=false",
                        "zy.web.trace.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CorsFilter.class);
                    assertThat(context).doesNotHaveBean(CorsConfigurationSource.class);
                    assertThat(context).doesNotHaveBean(WebLogAspect.class);
                    assertThat(context).doesNotHaveBean("traceIdFilter");
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                });
    }

    @Test
    void jacksonConvention_shouldApplyToSpringManagedMapper() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, WebAutoConfiguration.class))
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    assertThat(mapper.writeValueAsString(LocalDateTime.of(2026, 9, 10, 15, 0, 0)))
                            .isEqualTo("\"2026-09-10 15:00:00\"");
                    assertThat(mapper.writeValueAsString(9007199254741993L))
                            .isEqualTo("\"9007199254741993\"");
                });
    }
}
