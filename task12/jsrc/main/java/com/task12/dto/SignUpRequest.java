package com.task12.dto;

import org.json.JSONObject;

import java.util.regex.Pattern;

public record SignUpRequest(String email, String password, String firstName, String lastName) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE
    );

    public SignUpRequest {
        if (email == null || password == null || firstName == null || lastName == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
    }

    public static SignUpRequest fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String email = json.optString("email", null);
        String password = json.optString("password", null);
        String firstName = json.optString("firstName", null);
        String lastName = json.optString("lastName", null);
        if(!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        return new SignUpRequest(email, password, firstName, lastName);
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
