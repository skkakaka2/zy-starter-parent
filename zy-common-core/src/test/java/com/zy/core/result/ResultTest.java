package com.zy.core.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.core.config.JacksonConfig;
import com.zy.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    private final ObjectMapper mapper = JacksonConfig.objectMapper();

    @Test
    void ok_shouldContainCodeAndData() throws Exception {
        Result<String> r = Result.ok("hello");
        String json = mapper.writeValueAsString(r);
        assertThat(json).contains("\"code\":200").contains("\"data\":\"hello\"");
    }

    @Test
    void fail_shouldOmitNullTraceId() throws Exception {
        Result<?> r = Result.fail(ErrorCode.BIZ_ERROR);
        String json = mapper.writeValueAsString(r);
        assertThat(json).contains("\"code\":50001").doesNotContain("traceId");
    }
}
