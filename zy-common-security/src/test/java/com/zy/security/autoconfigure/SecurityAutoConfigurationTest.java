package com.zy.security.autoconfigure;

import com.zy.security.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class));

    @Test
    void backsOffCompletelyWhenJwtIsDisabled() {
        contextRunner.withPropertyValues("zy.security.jwt.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(JwtTokenService.class));
    }
}
