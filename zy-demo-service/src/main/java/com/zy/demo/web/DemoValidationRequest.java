package com.zy.demo.web;

import jakarta.validation.constraints.NotBlank;

/**
 * 示例请求体校验对象。
 */
public record DemoValidationRequest(
        @NotBlank(message = "name不能为空") String name) {
}
