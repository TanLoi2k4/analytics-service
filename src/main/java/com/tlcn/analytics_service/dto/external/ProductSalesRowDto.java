package com.tlcn.analytics_service.dto.external;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSalesRowDto {
    private Long productId;
    private Long quantity;
    private BigDecimal revenue;

    public ProductSalesRowDto() {
        super();
    }
}
