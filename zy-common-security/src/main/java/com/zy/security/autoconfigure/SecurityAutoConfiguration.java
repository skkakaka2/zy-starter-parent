package com.zy.security.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.security.filter.JwtAuthenticationFilter;
import com.zy.security.handler.UnAuthAccessDeniedHandler;
import com.zy.security.handler.UnAuthEntryPoint;
import com.zy.security.jwt.JwtConfigurationValidator;
import com.zy.security.jwt.JwtProperties;
import com.zy.security.jwt.JwtTokenService;
import com.zy.web.autoconfigure.WebAutoConfiguration;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Clock;
import java.util.List;

/**
 * zy-common-security 的 Servlet 自动配置入口。
 */
@AutoConfiguration
@AutoConfigureAfter(WebAutoConfiguration.class)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({HttpSecurity.class, Jwts.class})
@ConditionalOnProperty(prefix = JwtProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock jwtClock() {
        return Clock.systemUTC();
    }

    /**
     * 强制在自动配置启用时验证密钥；即使业务覆盖 TokenService 也不能静默接受缺失或弱密钥。
     */
    @Bean
    @ConditionalOnMissingBean(JwtConfigurationValidator.class)
    public JwtConfigurationValidator jwtConfigurationValidator(JwtProperties jwtProperties) {
        return new JwtConfigurationValidator(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean(JwtTokenService.class)
    public JwtTokenService jwtTokenService(JwtProperties jwtProperties,
                                           Clock jwtClock,
                                           JwtConfigurationValidator jwtConfigurationValidator) {
        return new JwtTokenService(jwtProperties, jwtClock);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public UnAuthEntryPoint unAuthEntryPoint(ObjectMapper objectMapper) {
        return new UnAuthEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public UnAuthAccessDeniedHandler unAuthAccessDeniedHandler(ObjectMapper objectMapper) {
        return new UnAuthAccessDeniedHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationFilter.class)
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                                            AuthenticationEntryPoint authenticationEntryPoint,
                                                            JwtProperties jwtProperties) {
        return new JwtAuthenticationFilter(jwtTokenService, authenticationEntryPoint, jwtProperties);
    }

    /**
     * JWT 过滤器仅由 Spring Security 链执行，禁止被 Servlet 容器再次注册。
     */
    @Bean
    @ConditionalOnMissingBean(name = "jwtAuthenticationFilterRegistration")
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<CorsConfigurationSource> corsConfigurationSource,
                                                   JwtProperties jwtProperties,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   AccessDeniedHandler accessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> corsConfigurationSource.ifUnique(cors::configurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(requestCache -> requestCache.requestCache(new NullRequestCache()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    List<String> permitPaths = jwtProperties.getPermitPaths();
                    if (!permitPaths.isEmpty()) {
                        authorize.requestMatchers(permitPaths.toArray(String[]::new)).permitAll();
                    }
                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
