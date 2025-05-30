package com.task03;

public class LambdaResponse {

    private Integer statusCode;

    private String message;

    public LambdaResponse(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
