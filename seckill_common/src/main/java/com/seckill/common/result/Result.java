package com.seckill.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果封装
 * <p>
 * 所有接口统一返回此格式，便于前端统一处理
 * </p>
 *
 * @param <T> 数据类型
 * @author seckill
 * @since 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 私有构造，使用静态方法创建实例
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回（无数据）
     *
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回（带数据）
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（自定义消息）
     *
     * @param message 返回消息
     * @param data    返回数据
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回（使用 ResultCode）
     *
     * @param resultCode 状态码枚举
     * @param <T>        数据类型
     * @return Result
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败返回（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回（使用 ResultCode，自定义消息）
     *
     * @param resultCode 状态码枚举
     * @param message    自定义消息
     * @param <T>        数据类型
     * @return Result
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return error(resultCode.getCode(), message);
    }

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 判断是否失败
     *
     * @return true-失败，false-成功
     */
    public boolean isFailed() {
        return !isSuccess();
    }
}
