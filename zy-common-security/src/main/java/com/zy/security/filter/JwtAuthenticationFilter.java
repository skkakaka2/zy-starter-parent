package com.zy.security.filter;

import com.zy.core.constant.HeaderConstants;
import com.zy.security.jwt.JwtProperties;
import com.zy.security.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Bearer JWT 认证过滤器。无 token 请求交由后续 Spring Security 授权规则处理。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer";

    private final JwtTokenService jwtTokenService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final List<String> permitPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.permitPaths = List.copyOf(jwtProperties.getPermitPaths());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = pathWithinApplication(request);
        return permitPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = bearerToken(request.getHeader(HeaderConstants.AUTHORIZATION));
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!StringUtils.hasText(token)) {
            reject(request, response);
            return;
        }

        try {
            String subject = jwtTokenService.parseSubject(token);
            Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuthentication == null || !currentAuthentication.isAuthenticated()) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken(subject, null, List.of()));
                SecurityContextHolder.setContext(context);
            }
            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            reject(request, response);
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid bearer token"));
    }

    /**
     * 返回 null 表示请求未携带 Bearer 凭证；空字符串表示 Bearer 格式不完整。
     */
    private String bearerToken(String authorization) {
        if (!StringUtils.hasText(authorization)
                || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        if (authorization.length() == BEARER_PREFIX.length()
                || !Character.isWhitespace(authorization.charAt(BEARER_PREFIX.length()))) {
            return "";
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        return requestUri.startsWith(contextPath) ? requestUri.substring(contextPath.length()) : requestUri;
    }
}
