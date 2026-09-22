package com.zy.web.autoconfigure;

import com.zy.web.config.CorsConfig;
import com.zy.web.config.TraceIdConfig;
import com.zy.web.config.ValidatorConfig;
import com.zy.web.config.WebJacksonConfig;
import com.zy.web.config.WebLogConfig;
import com.zy.web.exception.GlobalExceptionHandler;
import com.zy.web.props.WebProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * zy-common-web 自动装配入口。
 *
 * <p>通过 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 生效，
 * 业务服务无需额外 @ComponentScan("com.zy") 即可获得全部 Web 横切能力。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureBefore(ValidationAutoConfiguration.class)
@EnableConfigurationProperties(WebProperties.class)
@Import({
        WebJacksonConfig.class,
        ValidatorConfig.class,
        CorsConfig.class,
        TraceIdConfig.class,
        WebLogConfig.class
})
public class WebAutoConfiguration {

    /**
     * 全局异常处理器：以 Bean 形式注册，避免依赖组件扫描路径。
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
