package com.stu.helloserver.common; // 替换为你的实际项目包名

public class Result<T> {
    // 三个核心属性
    private String msg;
    private Integer code;
    private T data;

    // 静态工厂方法：成功回调（返回数据+成功状态）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.msg = ResultCode.SUCCESS.getMsg();
        result.data = data;
        return result;
    }

    // 静态工厂方法：失败回调（返回指定状态码+提示，无数据）
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.msg = resultCode.getMsg();
        result.data = null;
        return result;
    }

    // 自动生成的Getter和Setter方法（全属性）
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}