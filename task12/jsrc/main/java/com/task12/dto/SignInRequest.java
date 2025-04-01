package com.task12.dto;

import org.json.JSONObject;

public record SignInRequest(String email, String password) {

    public SignInRequest {
        if(email == null || password == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
    }

    public static SignInRequest fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        String email = jsonObject.optString("email");
        String password = jsonObject.optString("password");

        return new SignInRequest(email, password);
    }
}
