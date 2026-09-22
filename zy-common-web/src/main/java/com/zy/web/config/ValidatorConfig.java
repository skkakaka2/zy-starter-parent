package com.zy.web.config;

import com.zy.web.props.WebProperties;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 校验器配置：提供 Validation 的 Validator Bean，并支持 fail-fast。
 *
 * <p>必须早于 Boot 的 ValidationAutoConfiguration 装配（见 WebAutoConfiguration 的 @AutoConfigureBefore），
 * 否则 Boot 的 defaultValidator 会抢先注册，导致 failFast 配置失效。
 */
@Configuration(proxyBeanMethods = false)
public class ValidatorConfig {

    /** Hibernate 快速失败开关的属性名 */
    private static final String FAIL_FAST_PROPERTY = "hibernate.validator.fail_fast";

    @Bean
    @ConditionalOnMissingBean(Validator.class)
    public Validator validator(WebProperties webProperties) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        Map<String, String> validationProperties = new HashMap<>(2);
        validationProperties.put(FAIL_FAST_PROPERTY,
                String.valueOf(webProperties.getValidation().isFailFast()));
        factoryBean.setValidationPropertyMap(validationProperties);
        return factoryBean;
    }
}
