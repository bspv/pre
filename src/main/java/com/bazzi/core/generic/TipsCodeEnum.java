package com.bazzi.core.generic;

import lombok.Getter;

@Getter
public enum TipsCodeEnum {
    CODE_0000("0000", "成功"),

    // 客户端需要处理的情况
    CODE_0001("0001", "登录过期，请重新登录"),
    CODE_0002("0002", "请求参数有误，请检查"),
    CODE_0003("0003", "验签不通过，请检查"),
    CODE_0004("0004", "缺少header信息，请检查"),

    CODE_0011("0011", "系统异常，请稍后再试"),
    CODE_0012("0012", "访问ip不合法"),
    CODE_0013("0013", "数据异常,请联系客服"),
    CODE_0014("0014", "连接超时，请稍后再试"),
    CODE_0015("0015", "不支持的请求方式"),
    CODE_0016("0016", "异常的响应状态: %s"),
    CODE_0017("0017", "JSON序列化异常"),
    CODE_0018("0018", "缺少必要的参数，请检查"),
    CODE_0019("0019", "sign不能为空"),
    CODE_0020("0020", "该接口不支持`%s`方式请求"),
    CODE_0021("0021", "不能输入特殊字符，请重新输入"),

    ;


    private final String code;
    private final String message;

    TipsCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMsgByCode(String code) {
        for (TipsCodeEnum tipsCodeEnum : TipsCodeEnum.values()) {
            if (tipsCodeEnum.getCode().equals(code))
                return tipsCodeEnum.getMessage();
        }
        return "";
    }

}
