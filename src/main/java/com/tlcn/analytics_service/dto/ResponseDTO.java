package com.tlcn.analytics_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public ResponseDTO() {
        super();
    }

    public ResponseDTO(
        boolean success,
        String message,
        T data,
        String errorCode
    ) {
        super();
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    public ResponseDTO(boolean success, String message, T data) {
        this(success, message, data, null);
    }

    public static <T> ResponseDTO<T> ok(String message, T data) {
        return new ResponseDTO<>(true, message, data, null);
    }

    public static <T> ResponseDTO<T> fail(String message, String errorCode) {
        return new ResponseDTO<>(false, message, null, errorCode);
    }
}
