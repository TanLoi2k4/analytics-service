package com.tlcn.analytics_service.client;

import com.tlcn.analytics_service.dto.TopProductDTO;
import com.tlcn.analytics_service.dto.external.ProductServiceResponse;
import com.tlcn.analytics_service.dto.external.ProductSnapshotDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ProductClient {

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplate restTemplate) {
        super();
        this.restTemplate = restTemplate;
    }
    public ProductSnapshotDto getProduct(Long productId) {
        if (productId == null) {
            return null;
        }
        try {
            ResponseEntity<ProductServiceResponse<ProductSnapshotDto>> response = restTemplate.exchange(
                "http://product-service/products/{productId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ProductServiceResponse<ProductSnapshotDto>>() {},
                productId
            );
            ProductServiceResponse<ProductSnapshotDto> body = response.getBody();
            if (body == null || !body.isSuccess()) {
                return null;
            }
            return body.getData();
        } catch (RestClientException ex) {
            log.warn("Could not fetch product {} from product-service: {}", productId, ex.getMessage());
            return null;
        }
    }

    public TopProductDTO enrich(TopProductDTO product) {
        if (product == null || product.getProductId() == null) {
            return product;
        }
        ProductSnapshotDto snapshot = getProduct(product.getProductId());
        if (snapshot == null) {
            return product;
        }
        if (snapshot.getName() != null && !snapshot.getName().isBlank()) {
            product.setName(snapshot.getName());
        }
        product.setStock(snapshot.getStock());
        product.setRating(snapshot.getRating());
        String imageUrl = snapshot.resolveImageUrl();
        if (imageUrl != null) {
            product.setImageUrl(imageUrl);
        }
        if (snapshot.getCategory() != null && snapshot.getCategory().getName() != null) {
            product.setCategoryName(snapshot.getCategory().getName());
        }
        return product;
    }
}
