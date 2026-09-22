package com.zy.web.desensitize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.core.config.JacksonConfig;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 脱敏序列化验证。
 */
class DesensitizeSerializerTest {

    private final ObjectMapper mapper = JacksonConfig.objectMapper();

    @Test
    void shouldMaskByType() throws Exception {
        UserVO vo = new UserVO();

        String json = mapper.writeValueAsString(vo);

        assertThat(json).contains("\"phone\":\"138****5678\"");
        assertThat(json).contains("\"name\":\"张*\"");
        assertThat(json).contains("\"idCard\":\"110101********1234\"");
        assertThat(json).contains("\"email\":\"z***@example.com\"");
        assertThat(json).contains("\"password\":\"******\"");
        assertThat(json).contains("\"plain\":\"明文\"");
    }

    @Test
    void shouldMaskByCustomRule() throws Exception {
        CustomVO vo = new CustomVO();
        vo.setSecret("abcdefg");

        String json = mapper.writeValueAsString(vo);

        assertThat(json).contains("\"secret\":\"abc**fg\"");
    }

    @Test
    void nullValue_shouldWriteNull() throws Exception {
        CustomVO vo = new CustomVO();

        assertThat(mapper.writeValueAsString(vo)).contains("\"secret\":null");
    }

    @Data
    static class UserVO {

        @Desensitize(DesensitizeType.PHONE)
        private String phone = "13812345678";

        @Desensitize(DesensitizeType.NAME)
        private String name = "张三";

        @Desensitize(DesensitizeType.ID_CARD)
        private String idCard = "110101199003071234";

        @Desensitize(DesensitizeType.EMAIL)
        private String email = "zhangsan@example.com";

        @Desensitize(DesensitizeType.PASSWORD)
        private String password = "abc123";

        private String plain = "明文";
    }

    @Data
    static class CustomVO {

        @Desensitize(value = DesensitizeType.CUSTOM, prefixKeep = 3, suffixKeep = 2)
        private String secret;
    }
}
