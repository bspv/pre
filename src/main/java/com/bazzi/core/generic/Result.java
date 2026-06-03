package com.bazzi.core.generic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@ApiModel(value = "Result")
public final class Result<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 8045430280753627924L;

    @ApiModelProperty(value = "数据")
    private T data;// 数据

    @Builder.Default
    @ApiModelProperty(value = "是否成功")
    private boolean status = true;

    @ApiModelProperty(value = "状态码")
    private String code;// 状态码

    @ApiModelProperty(value = "提示信息")
    private String message;// 提示信息

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

    /**
     * 基于错误提示枚举构建失败信息
     *
     * @param tipsCodeEnum 错误提示信息
     * @param <T>          泛型类型
     * @return 失败结果
     */
    public static <T extends Serializable> Result<T> failure(TipsCodeEnum tipsCodeEnum) {
        return Result.<T>builder().status(false).code(tipsCodeEnum.getCode()).message(tipsCodeEnum.getMessage()).build();
    }

}
