package com.bazzi.core.generic;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public final class Result<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 4119708214034627331L;
    private T data;
    private boolean status = true;
    private String message = "";
    private String code = "";

    /**
     * 构建一个data数据的成功结果
     *
     * @param data 数据
     * @param <T>  泛型类型
     * @return 成功结果
     */
    public static <T extends Serializable> Result<T> success(T data) {
        return Result.<T>builder().status(true).data(data).build();
    }

    /**
     * 构建一个以错误码和提示信息的失败结果
     *
     * @param code    错误码
     * @param message 提示信息
     * @param <T>     泛型类型
     * @return 失败结果
     */
    public static <T extends Serializable> Result<T> failure(String code, String message) {
        return Result.<T>builder().status(false).code(code).message(message).build();
    }

}
