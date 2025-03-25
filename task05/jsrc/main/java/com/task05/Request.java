package com.task05;

public class Request {

    private Integer principalId;

    private Content content;

    public Request(Integer principalId, Content person) {
        this.principalId = principalId;
        this.content = person;
    }

    public Request() {
    }

    public Integer getPrincipalId() {
        return principalId;
    }

    public Content getContent() {
        return content;
    }

    public void setPrincipalId(Integer principalId) {
        this.principalId = principalId;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Request{" +
                "principalId=" + principalId +
                ", content=" + content +
                '}';
    }
}
