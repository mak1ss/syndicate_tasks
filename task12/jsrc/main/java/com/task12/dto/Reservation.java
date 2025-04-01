package com.task12.dto;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class Reservation {

    private Integer tableNumber;
    private String clientName;
    private String phoneNumber;
    private LocalDate date;
    private LocalTime slotTimeStart;
    private LocalTime slotTimeEnd;

    public Reservation() {
    }

    public Reservation(Map<String, AttributeValue> attributes) {
        this.tableNumber = Integer.valueOf(attributes.get("tableNumber").getN());
        this.clientName = attributes.get("clientName").getS();
        this.phoneNumber = attributes.get("phoneNumber").getS();
        this.date = LocalDate.parse(attributes.get("date").getS());
        this.slotTimeStart = LocalTime.parse(attributes.get("slotTimeStart").getS());
        this.slotTimeEnd = LocalTime.parse(attributes.get("slotTimeEnd").getS());
    }

    public static Reservation fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        Reservation reservation = new Reservation();

        reservation.setTableNumber(jsonObject.getInt("tableNumber"));
        reservation.setClientName(jsonObject.getString("clientName"));
        reservation.setPhoneNumber(jsonObject.getString("phoneNumber"));
        reservation.setDate(LocalDate.parse(jsonObject.getString("date")));
        reservation.setSlotTimeStart(LocalTime.parse(jsonObject.getString("slotTimeStart")));
        reservation.setSlotTimeEnd(LocalTime.parse(jsonObject.getString("slotTimeEnd")));

        return reservation;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getSlotTimeStart() {
        return slotTimeStart;
    }

    public void setSlotTimeStart(LocalTime slotTimeStart) {
        this.slotTimeStart = slotTimeStart;
    }

    public LocalTime getSlotTimeEnd() {
        return slotTimeEnd;
    }

    public void setSlotTimeEnd(LocalTime slotTimeEnd) {
        this.slotTimeEnd = slotTimeEnd;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "tableNumber=" + tableNumber +
                ", clientName='" + clientName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", date=" + date +
                ", slotTimeStart=" + slotTimeStart +
                ", slotTimeEnd=" + slotTimeEnd +
                '}';
    }
}
