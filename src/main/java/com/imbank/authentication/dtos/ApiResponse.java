package com.imbank.authentication.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private String code = "0";
    private String status = "success";
    private String message;
    private T data;

    public ApiResponse(String message) {
        this(message, null);
    }

    public ApiResponse(String message, T data) {
        this("0", message, data);
    }
    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return "0".equals(code) ? "success" : "error";
    }
}
