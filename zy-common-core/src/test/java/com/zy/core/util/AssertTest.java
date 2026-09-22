package com.zy.core.util;

import com.zy.core.exception.BusinessException;
import com.zy.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssertTest {

    @Test
    void notNull_shouldThrowWithCode() {
        assertThatThrownBy(() -> Assert.notNull(null, ErrorCode.BIZ_ERROR))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(50001));
    }

    @Test
    void isTrue_shouldNotThrow() {
        Assert.isTrue(true, ErrorCode.BIZ_ERROR);
    }
}
