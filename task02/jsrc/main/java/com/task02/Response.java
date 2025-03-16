package com.task02;

public class Response {

    private Integer statusCode;

    private String body;

    public Response(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.body = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
