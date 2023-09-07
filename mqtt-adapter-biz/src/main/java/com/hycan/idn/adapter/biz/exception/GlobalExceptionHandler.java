package com.hycan.idn.adapter.biz.exception;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.Tracer;
import com.hycan.idn.tsp.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求方式异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R validationParamException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方式错误：{}", e.getMessage(), e);
        return R.failed("请求方式错误," + e.getMessage());
    }

    /**
     * 请求Body格式异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R notReadableExceptionException(HttpMessageNotReadableException e) {
        log.error("请求Body内容错误：{}", e.getHttpInputMessage(), e);
        return R.failed("请求参数格式异常");
    }

    /**
     * RequestParam 校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R validationParamException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        ConstraintViolation<?> violation = new ArrayList<>(violations).get(0);
        String[] split = violation.getPropertyPath().toString().split("\\.");
        String paramName = split[split.length - 1];
        String err = paramName + violation.getMessage();
        log.error("请求参数异常：{}", err, e);
        return R.failed(err);
    }

    /**
     *  Validated 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleValidParamException(MethodArgumentNotValidException e) {
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        List<String> collect = errors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        String message = StrUtil.join(StrUtil.COMMA, collect);
        log.error("请求body异常：{}", message, e);
        return R.failed(message);
    }

    /**
     *  文件上传大小校验异常
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleValidMultipartFileException(MultipartException e) {
        String message = "上传文件异常";
        log.error("请求body异常：{}", message, e);
        return R.failed(message);
    }

    @ExceptionHandler(AdapterBusinessException.class)
    public R handleBusinessException(AdapterBusinessException e) {
        log.error("业务异常 ex={}", e.getMessage(), e);
        Tracer.trace(e);
        return R.restResult(e.getData(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public R handleError(Throwable e) {
        log.error("全局异常信息 ex={}", e.getMessage(), e);
        Tracer.trace(e);
        return R.failed(e.getMessage());
    }

}
