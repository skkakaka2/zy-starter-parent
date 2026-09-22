package com.zy.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    private final ObjectMapper mapper = JacksonConfig.objectMapper();

    @Test
    void localDateTime_shouldFormat() throws Exception {
        assertThat(mapper.writeValueAsString(LocalDateTime.of(2026, 9, 10, 15, 0, 0)))
                .isEqualTo("\"2026-09-10 15:00:00\"");
    }

    @Test
    void localDateTime_shouldDeserialize() throws Exception {
        LocalDateTime dt = mapper.readValue("\"2026-09-10 15:00:00\"", LocalDateTime.class);
        assertThat(dt).isEqualTo(LocalDateTime.of(2026, 9, 10, 15, 0, 0));
    }

    @Test
    void long_shouldSerializeAsString() throws Exception {
        assertThat(mapper.writeValueAsString(9007199254741993L)).isEqualTo("\"9007199254741993\"");
    }

    @Test
    void unknownProperty_shouldBeIgnored() throws Exception {
        Demo d = mapper.readValue("{\"name\":\"a\",\"unknown\":1}", Demo.class);
        assertThat(d.name).isEqualTo("a");
    }

    public static class Demo {
        public String name;
    }
}
