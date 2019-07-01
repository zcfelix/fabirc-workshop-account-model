package com.thoughtworks.fabric.workshop.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChaincodeResponse {
    private String message;
    private String code;
    private boolean isSuccess;

    public ChaincodeResponse(String message, String code, boolean isSuccess) {
        this.message = message;
        this.code = code;
        this.isSuccess = isSuccess;
    }

    public static String success(String successMessage) {
        try {
            return new ObjectMapper().writeValueAsString(new ChaincodeResponse(successMessage, "", true));
        } catch (JsonProcessingException e) {
            return String.format("{\"message\":'%s BUT %s (NO COMMIT)', \"OK\":%s}", e.getMessage(), successMessage, false);
        }
    }

    public static String error(String errorMessage, String code) {
        try {
            return new ObjectMapper().writeValueAsString(new ChaincodeResponse(errorMessage, code, false));
        } catch (JsonProcessingException e) {
            return String.format("{\"code\":'%s', \"message\":'%s AND %s', \"OK\":%s}", code, e.getMessage(), errorMessage, false);
        }
    }
}