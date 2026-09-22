package com.zy.web.exception;

import com.zy.core.exception.BusinessException;
import com.zy.core.exception.ErrorCode;
import com.zy.core.exception.SystemException;
import com.zy.core.result.Result;
import com.zy.web.trace.TraceIdContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：所有异常统一转成 {@link Result}，并回填 traceId。
 *
 * <p>HTTP 状态约定：
 * <ul>
 *   <li>业务异常（BusinessException）：HTTP 200 + 业务码，业务失败属可预期结果、非协议错误；</li>
 *   <li>系统异常 / 兜底异常：HTTP 500；</li>
 *   <li>参数类异常：HTTP 400（方法不支持 405、资源不存在 404）。</li>
 * </ul>
 *
 * <p>Spring 数据访问异常映射由 Web 层统一负责；MyBatis 模块保持对 Web 的独立性，不反向依赖 Web。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常 code={}, message={}", e.getCode(), e.getMessage());
        return build(HttpStatus.OK, e.getCode(), e.getMessage());
    }

    /**
     * 系统异常。
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Void>> handleSystemException(SystemException e) {
        log.error("系统异常 code={}, message={}", e.getCode(), e.getMessage(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getCode(), e.getMessage());
    }

    /**
     * 唯一键冲突。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一键冲突", e);
        return build(HttpStatus.CONFLICT, ErrorCode.CONFLICT.getCode(), ErrorCode.CONFLICT.getMessage());
    }

    /**
     * 其他数据完整性约束冲突。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性约束冲突", e);
        return build(HttpStatus.CONFLICT, ErrorCode.CONFLICT.getCode(), ErrorCode.CONFLICT.getMessage());
    }

    /**
     * 其他数据访问异常，不向客户端暴露底层异常信息。
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> handleDataAccessException(DataAccessException e) {
        log.error("数据访问异常", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

    /**
     * @RequestBody @Valid 校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 表单绑定校验失败（非 JSON 提交）。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * Spring 6.1+ 方法级校验失败（如 @RequestParam 上加 @NotBlank）。
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Result<Void>> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String message = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ":" + resolveMessage(error)))
                .collect(Collectors.joining("; "));
        log.warn("方法参数校验失败：{}", message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 手动 validate 抛出的约束违规。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败：{}", message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 缺少必填参数。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = "缺少必填参数：" + e.getParameterName();
        log.warn(message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 参数类型不匹配。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        Object value = e.getValue();
        String message = "参数类型不匹配：" + e.getName() + "=" + value
                + (e.getRequiredType() == null ? "" : "（应为 " + e.getRequiredType().getSimpleName() + "）");
        log.warn(message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 请求体不可读（JSON 格式错误）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误，无法解析");
    }

    /**
     * 请求方法不支持。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String method = e.getMethod();
        String message = "请求方法不支持：" + method;
        log.warn(message);
        return build(HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED.value(), message);
    }

    /**
     * 静态资源不存在（Boot 3.2+ 默认抛 NoResourceFoundException）。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源不存在：{}", e.getResourcePath());
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.getCode(), "资源不存在：" + e.getResourcePath());
    }

    /**
     * 处理器不存在（需开启 spring.mvc.throw-exception-if-no-handler-found）。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在：{}", e.getRequestURL());
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.getCode(), "接口不存在：" + e.getRequestURL());
    }

    /**
     * 兜底：任何未预期异常，返回 500 且不泄漏堆栈细节。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未捕获异常", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage();
        return fieldError.getField() + ":" + (defaultMessage == null ? "校验失败" : defaultMessage);
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        String property = String.valueOf(violation.getPropertyPath());
        return property + ":" + violation.getMessage();
    }

    private String resolveMessage(MessageSourceResolvable error) {
        String defaultMessage = error.getDefaultMessage();
        return defaultMessage == null ? "校验失败" : defaultMessage;
    }

    private ResponseEntity<Result<Void>> build(HttpStatus httpStatus, int code, String message) {
        Result<Void> result = Result.<Void>fail(code, message);
        result.setTraceId(TraceIdContext.getTraceId());
        return ResponseEntity.status(httpStatus).body(result);
    }
}
