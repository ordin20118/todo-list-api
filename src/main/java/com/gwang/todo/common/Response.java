package com.gwang.todo.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
public class Response {

    private boolean success;
    private HttpStatus httpStatus;
    private String message;
    private Object data;

    public Response(){}
    public static Response successInstance(){
        return new Response().success();
    }

    public Response success(){
        httpStatus = HttpStatus.OK;
        success = true;
        message = "success";
        return this;
    }

    public Response fail(){
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        success = false;
        message = "Internal server error.";
        return this;
    }

    public int getHttpStatus(){
        return httpStatus.value();
    }

}
