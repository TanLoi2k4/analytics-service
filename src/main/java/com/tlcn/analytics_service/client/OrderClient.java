package com.tlcn.analytics_service.client;

import com.tlcn.analytics_service.dto.external.ProductSalesRowDto;
import com.tlcn.analytics_service.dto.external.ProductServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class OrderClient {

    private final RestTemplate restTemplate;

    public OrderClient(RestTemplate restTemplate) {
        super();
        this.restTemplate = restTemplate;
    }
    public List<ProductSalesRowDto> getVendorProductSales(
        String vendorKeycloakId,
        LocalDate from,
        LocalDate to,
        int limit
    ) {
        if (vendorKeycloakId == null || vendorKeycloakId.isBlank() || from == null || to == null) {
            return List.of();
        }
        try {
            ResponseEntity<ProductServiceResponse<List<ProductSalesRowDto>>> response = restTemplate.exchange(
                "http://order-service/orders/internal/vendors/{vendorId}/product-sales?from={from}&to={to}&limit={limit}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ProductServiceResponse<List<ProductSalesRowDto>>>() {},
                vendorKeycloakId,
                from,
                to,
                Math.max(limit, 1)
            );
            ProductServiceResponse<List<ProductSalesRowDto>> body = response.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                return List.of();
            }
            return body.getData();
        } catch (RestClientException ex) {
            log.warn(
                "Could not fetch product sales for vendor {} from order-service: {}",
                vendorKeycloakId,
                ex.getMessage()
            );
            return null;
        }
    }

    public List<ProductSalesRowDto> getPlatformProductSales(LocalDate from, LocalDate to, int limit) {
        if (from == null || to == null) {
            return List.of();
        }
        try {
            ResponseEntity<ProductServiceResponse<List<ProductSalesRowDto>>> response = restTemplate.exchange(
                "http://order-service/orders/internal/product-sales?from={from}&to={to}&limit={limit}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ProductServiceResponse<List<ProductSalesRowDto>>>() {},
                from,
                to,
                Math.max(limit, 1)
            );
            ProductServiceResponse<List<ProductSalesRowDto>> body = response.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                return List.of();
            }
            return body.getData();
        } catch (RestClientException ex) {
            log.warn("Could not fetch platform product sales from order-service: {}", ex.getMessage());
            return null;
        }
    }
}
