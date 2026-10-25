package com.tlcn.analytics_service.dto.external;

import lombok.Data;

@Data
public class ProductServiceResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ProductServiceResponse() {
        super();
    }
}
