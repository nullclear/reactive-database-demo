package dev.yxy.reactive.model.domain;

import java.io.Serializable;

public class ResponseData implements Serializable {
    private static final long serialVersionUID = 3053961262876371847L;

    private boolean success = true;
    private Integer code;
    private String msg;
    private Object data;

    public ResponseData() {
    }

    public ResponseData(boolean success, Integer code) {
        this.success = success;
        this.code = code;
    }

    public ResponseData(boolean success, Integer code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public ResponseData(boolean success, Integer code, String msg, Object data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{"
                + "\"success\":"
                + success
                + ",\"code\":"
                + code
                + ",\"msg\":\""
                + msg + '\"'
                + ",\"data\":"
                + data
                + "}";
    }
}
