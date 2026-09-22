package com.zy.web.exception;

import com.zy.core.exception.BusinessException;
import com.zy.core.exception.ErrorCode;
import com.zy.core.exception.SystemException;
import com.zy.web.trace.TraceIdContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全局异常转 Result 的验证（standalone MockMvc，不启动容器）。
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        TraceIdContext.clear();
    }

    @Test
    void businessException_shouldReturnHttp200WithBizCode() throws Exception {
        mockMvc.perform(get("/test/biz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(50001))
                .andExpect(jsonPath("$.message").value("余额不足"));
    }

    @Test
    void systemException_shouldReturnHttp500() throws Exception {
        mockMvc.perform(get("/test/sys"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_ERROR.getMessage()));
    }

    @Test
    void unexpectedException_shouldReturnHttp500() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void duplicateKeyException_shouldReturnHttp409WithStableConflictMessage() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONFLICT.getMessage()));
    }

    @Test
    void dataIntegrityViolationException_shouldReturnHttp409WithConflictCode() throws Exception {
        mockMvc.perform(get("/test/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONFLICT.getMessage()));
    }

    @Test
    void dataAccessException_shouldReturnHttp500WithGenericMessage() throws Exception {
        mockMvc.perform(get("/test/data-access"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_ERROR.getMessage()));
    }

    @Test
    void bodyValidationError_shouldReturnHttp400WithFieldMessage() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_VALID_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("name:姓名不能为空"));
    }

    @Test
    void missingParameter_shouldReturnHttp400() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_VALID_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("缺少必填参数：name"));
    }

    @Test
    void typeMismatch_shouldReturnHttp400() throws Exception {
        mockMvc.perform(get("/test/number").param("value", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_VALID_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("参数类型不匹配")));
    }

    @Test
    void methodNotSupported_shouldReturnHttp405() throws Exception {
        mockMvc.perform(get("/test/valid"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405));
    }

    @Test
    void result_shouldCarryTraceIdFromMdc() throws Exception {
        TraceIdContext.setTraceId("tid-test-001");
        mockMvc.perform(get("/test/biz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").value("tid-test-001"));
    }

    /**
     * 测试用控制器。
     */
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/biz")
        public String biz() {
            throw new BusinessException(ErrorCode.BIZ_ERROR, "余额不足");
        }

        @GetMapping("/sys")
        public String sys() {
            throw new SystemException(ErrorCode.INTERNAL_ERROR);
        }

        @GetMapping("/error")
        public String error() {
            throw new IllegalStateException("boom");
        }

        @GetMapping("/duplicate")
        public String duplicate() {
            throw new DuplicateKeyException("Duplicate entry 'secret@example.com' for key 'user_email'");
        }

        @GetMapping("/integrity")
        public String integrity() {
            throw new DataIntegrityViolationException("违反约束：用户私密信息");
        }

        @GetMapping("/data-access")
        public String dataAccess() {
            throw new DataAccessException("数据库连接密码泄漏") {
            };
        }

        @PostMapping("/valid")
        public String valid(@Valid @RequestBody DemoDTO dto) {
            return dto.getName();
        }

        @GetMapping("/param")
        public String param(@RequestParam("name") String name) {
            return name;
        }

        @GetMapping("/number")
        public String number(@RequestParam("value") Integer value) {
            return String.valueOf(value);
        }
    }

    @Data
    static class DemoDTO {

        @NotBlank(message = "姓名不能为空")
        private String name;
    }
}
