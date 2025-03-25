package com.task05;

public class Request {

    private Integer principalId;

    private Person content;

    public Request(Integer principalId, Person person) {
        this.principalId = principalId;
        this.content = person;
    }

    public Request() {
    }

    public Integer getPrincipalId() {
        return principalId;
    }

    public Person getContent() {
        return content;
    }

    public void setPrincipalId(Integer principalId) {
        this.principalId = principalId;
    }

    public void setContent(Person content) {
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
