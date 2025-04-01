package com.task12.dto;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.json.JSONObject;
import org.json.JSONPropertyName;

import java.util.Map;

public class Table {

    private Integer id;
    private Integer number;
    private Integer places;
    private Boolean isVip;
    private Integer minOrder;

    public Table() {
    }

    public Table(Map<String, AttributeValue> attributes) {
        this.id = Integer.valueOf(attributes.get("id").getS());
        this.number = Integer.valueOf(attributes.get("number").getN());
        this.places = Integer.valueOf(attributes.get("places").getN());
        this.isVip = attributes.get("isVip").getBOOL();
        var minOrderAttribute = attributes.get("minOrder");
        if(minOrderAttribute != null){
            this.minOrder = Integer.valueOf(minOrderAttribute.getN());
        }
    }

    public static Table fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        Table table = new Table();

        table.setId(jsonObject.getInt("id"));
        table.setNumber(jsonObject.getInt("number"));
        table.setPlaces(jsonObject.getInt("places"));
        table.setVip(jsonObject.getBoolean("isVip"));

        if(jsonObject.has("minOrder")){
            table.setMinOrder(jsonObject.getInt("minOrder"));
        }

        return table;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getPlaces() {
        return places;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }

    @JSONPropertyName("isVip")
    public Boolean getVip() {
        return isVip;
    }

    public void setVip(Boolean vip) {
        isVip = vip;
    }

    public Integer getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(Integer minOrder) {
        this.minOrder = minOrder;
    }
}
