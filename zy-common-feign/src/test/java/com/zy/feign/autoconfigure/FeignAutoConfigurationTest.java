package com.zy.feign.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zy.feign.decoder.FeignErrorDecoder;
import com.zy.feign.interceptor.FeignRequestInterceptor;

/**
 * Feign 自动配置的无远程调用测试。
 */
class FeignAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FeignAutoConfiguration.class));

    @Test
    void registersExpectedDefaults() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(FeignRequestInterceptor.class);
            assertThat(context).hasSingleBean(FeignErrorDecoder.class);
            assertThat(context).hasSingleBean(Request.Options.class);
            assertThat(context.getBean(Request.Options.class).connectTimeoutMillis()).isEqualTo(2_000);
            assertThat(context.getBean(Request.Options.class).readTimeoutMillis()).isEqualTo(5_000);
            assertThat(context.getBean(Retryer.class)).isSameAs(Retryer.NEVER_RETRY);
            assertThat(context.getBean(Logger.Level.class)).isEqualTo(Logger.Level.BASIC);
        });
    }

    @Test
    void backsOffForUserErrorDecoder() {
        contextRunner.withUserConfiguration(UserErrorDecoderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ErrorDecoder.class);
                    assertThat(context.getBean(ErrorDecoder.class)).isSameAs(UserErrorDecoderConfiguration.ERROR_DECODER);
                });
    }

    @Test
    void keepsHeaderPropagationWhenApplicationAddsAnotherInterceptor() {
        contextRunner.withUserConfiguration(UserRequestInterceptorConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(FeignRequestInterceptor.class);
                    assertThat(context.getBeansOfType(RequestInterceptor.class)).hasSize(2);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserErrorDecoderConfiguration {

        private static final ErrorDecoder ERROR_DECODER = (methodKey, response) -> new IllegalStateException("custom");

        @Bean
        ErrorDecoder userErrorDecoder() {
            return ERROR_DECODER;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserRequestInterceptorConfiguration {

        @Bean
        RequestInterceptor userRequestInterceptor() {
            return template -> template.header("X-Application-Header", "present");
        }
    }
}
