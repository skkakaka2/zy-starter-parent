package com.zy.security;

import com.zy.security.filter.JwtAuthenticationFilter;
import com.zy.security.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthoritySecurityIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class AuthoritySecurityIntegrationTest {

    private static final String TEST_SECRET = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("test-only-signing-key-material-000".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ApplicationContext applicationContext;

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("zy.security.jwt.secret", () -> TEST_SECRET);
    }

    @Test
    void customSecurityFilterChainBacksOffDefaultAndUsesJsonForbiddenHandler() throws Exception {
        assertThat(applicationContext.getBeansOfType(SecurityFilterChain.class)).hasSize(1);
        String token = jwtTokenService.issueToken("user-42");

        mockMvc.perform(get("/secure/admin").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限"));
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import({TestController.class, TestSecurityConfiguration.class})
    static class TestApplication {
    }

    @Configuration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain authoritySecurityFilterChain(HttpSecurity http,
                                                         CorsConfigurationSource corsConfigurationSource,
                                                         JwtAuthenticationFilter jwtAuthenticationFilter,
                                                         AuthenticationEntryPoint authenticationEntryPoint,
                                                         AccessDeniedHandler accessDeniedHandler) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .requestCache(requestCache -> requestCache.requestCache(new NullRequestCache()))
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/secure/admin").hasAuthority("ADMIN")
                            .anyRequest().permitAll())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/secure/admin")
        String authorityEndpoint() {
            return "admin";
        }
    }
}
