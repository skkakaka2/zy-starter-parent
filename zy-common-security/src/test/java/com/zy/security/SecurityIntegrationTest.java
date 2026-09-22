package com.zy.security;

import com.zy.security.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SecurityIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    private static final String TEST_SECRET = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("test-only-signing-key-material-000".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("zy.security.jwt.secret", () -> TEST_SECRET);
        registry.add("zy.web.cors.allowed-origins", () -> "https://client.example");
    }

    @Test
    void permitsPublicRouteAndRejectsProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("public"));

        mockMvc.perform(get("/secure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void authenticatesValidTokenAndRejectsInvalidBearerToken() throws Exception {
        String token = jwtTokenService.issueToken("user-42");

        mockMvc.perform(get("/secure").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("user-42"));

        mockMvc.perform(get("/secure").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证"));
    }

    @Test
    void handlesCorsPreflightWithCommonWebPolicy() throws Exception {
        mockMvc.perform(options("/secure")
                        .header("Origin", "https://client.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://client.example"));
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(TestController.class)
    static class TestApplication {
    }

    @RestController
    static class TestController {

        @GetMapping("/auth/ping")
        Map<String, String> publicEndpoint() {
            return Map.of("value", "public");
        }

        @GetMapping("/secure")
        Map<String, String> protectedEndpoint(org.springframework.security.core.Authentication authentication) {
            return Map.of("value", authentication.getName());
        }
    }
}
