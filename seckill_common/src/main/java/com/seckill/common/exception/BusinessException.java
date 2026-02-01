package com.seckill.common.exception;

import com.seckill.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑中主动抛出的异常，会被全局异常处理器捕获并返回友好提示
 * </p>
 *
 * @author seckill
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 使用 ResultCode 构造
     *
     * @param resultCode 状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 使用自定义状态码和消息构造
     *
     * @param code    状态码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 使用 ResultCode 和自定义消息构造
     *
     * @param resultCode 状态码枚举
     * @param message    自定义消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * 使用 ResultCode 和原始异常构造
     *
     * @param resultCode 状态码枚举
     * @param cause      原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
}
