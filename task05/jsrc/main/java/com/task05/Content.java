package com.task05;


import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Map;

public class Content {

    private Map<String, AttributeValue> values;

    public Content(Map<String, AttributeValue> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Content{" +
                "values=" + values +
                '}';
    }
}
