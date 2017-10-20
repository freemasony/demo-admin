package com.demo.web.controller.vo;


import com.demo.common.util.JsonSerializeUtil;

import java.io.Serializable;

/**
 * Created by lijun
 * Date: 2014/12/31
 * Time: 11:21
 */
public class ResultInfo<T> implements Serializable{
    private static final long serialVersionUID = 1552145865566665L;
    private int success;//api调用是否成功
    private int status;//返回状态码;正确为200;包含异常错误为500
    private String code;
    private T data;//返回结果数据集
    private String exception;//异常信息;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String toJsonString(){
        return JsonSerializeUtil.objectToJson(this);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSuccessData(T data){
        this.setCode("success");
        this.setSuccess(1);
        this.setStatus(200);
        this.setData(data);
    }

    public void setSuccessData(T data,int status){
        this.setCode("success");
        this.setSuccess(1);
        this.setStatus(status);
        this.setData(data);
    }

    public void setExceptionData(String exception){
        this.setSuccess(0);
        this.setStatus(500);
        this.setData(null);
        this.setException(exception);
    }

}
