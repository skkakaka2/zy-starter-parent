package com.zy.demo;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.zy.core.constant.HeaderConstants;
import com.zy.security.jwt.JwtTokenService;

/**
 * 示例服务端到端 MockMvc 测试，不依赖 Redis 或 Nacos 服务。
 */
@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationIntegrationTest {

    private static final String TEST_SECRET = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("zy.security.jwt.secret", () -> TEST_SECRET);
    }

    @Test
    void healthIsPublicAndReturnsTraceHeader() throws Exception {
        mockMvc.perform(get("/actuator/health").header(HeaderConstants.TRACE_ID, "trace-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(header().string(HeaderConstants.TRACE_ID, "trace-health"));
    }

    @Test
    void unauthenticatedDemoRequestReturnsJson401AndTrace() throws Exception {
        mockMvc.perform(get("/demo/hello").header(HeaderConstants.TRACE_ID, "trace-unauthenticated"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HeaderConstants.TRACE_ID, "trace-unauthenticated"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.traceId").value("trace-unauthenticated"));
    }

    @Test
    void authenticatedHelloReturnsGreeting() throws Exception {
        mockMvc.perform(get("/demo/hello")
                        .header(HeaderConstants.AUTHORIZATION, bearerToken())
                        .header(HeaderConstants.TRACE_ID, "trace-authenticated"))
                .andExpect(status().isOk())
                .andExpect(header().string(HeaderConstants.TRACE_ID, "trace-authenticated"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("hello"));
    }

    @Test
    void invalidValidationRequestReturns40001AndTrace() throws Exception {
        mockMvc.perform(post("/demo/validate")
                        .header(HeaderConstants.AUTHORIZATION, bearerToken())
                        .header(HeaderConstants.TRACE_ID, "trace-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HeaderConstants.TRACE_ID, "trace-validation"))
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.traceId").value("trace-validation"));
    }

    @Test
    void pageReturnsAtLeastThreeTotalRecordsAndTwoRows() throws Exception {
        mockMvc.perform(get("/demo/page")
                        .param("pageNum", "1")
                        .param("pageSize", "2")
                        .header(HeaderConstants.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(3), Integer.class))
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }

    @Test
    void springdocEndpointsArePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    private String bearerToken() {
        return "Bearer " + jwtTokenService.issueToken("demo-user");
    }
}
