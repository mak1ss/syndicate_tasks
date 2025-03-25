package com.task05;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Map;
import java.util.stream.Collectors;

public class Response {

    private Integer statusCode;

    private Event event;

    public Response(Integer statusCode, Event event) {
        this.statusCode = statusCode;
        this.event = event;
    }

    public Response() {
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Event getEvent() {
        return event;
    }

    public static class Event {

        private String id;

        private Integer principalId;

        private String createdAt;

        private Map<String, String> body;

        public Event(String id, Integer principalId, String createdAt, Map<String, String> content) {
            this.id = id;
            this.principalId = principalId;
            this.createdAt = createdAt;
            this.body = content;
        }

        public Event(Map<String, AttributeValue> items) {
            this.id = items.get("id").getS();
            this.principalId = Integer.valueOf(items.get("principalId").getN());
            this.createdAt = items.get("createdAt").getS();
            Map<String, AttributeValue> bodyMap = items.get("body").getM();
            this.body = bodyMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getS()
                    ));
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setPrincipalId(Integer principalId) {
            this.principalId = principalId;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public Integer getPrincipalId() {
            return principalId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public Map<String, String> getBody() {
            return body;
        }

        public void setBody(Map<String, String> body) {
            this.body = body;
        }
    }
}
