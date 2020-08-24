package com.bazzi.core.generic;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = -5114827930099526751L;
    private T data;
    private boolean success = true;
    private String message = "";
    private String code = "";

    public Result() {
    }

    public Result(T data) {
        super();
        this.data = data;
    }

    public void setError(String code, String message) {
        this.code = code;
        this.message = message;
        this.success = false;
    }

    /**
     * 构建一个data数据的成功结果
     *
     * @param data 数据
     * @param <T>  泛型类型
     * @return 成功结果
     */
    public static <T extends Serializable> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
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
        Result<T> result = new Result<>();
        result.setError(code, message);
        return result;
    }

}
