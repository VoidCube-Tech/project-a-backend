package com.voidcube.tech.projectA.product.dto.response;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ProductImageFileResponseDTO(
    Resource resource,
    MediaType mediaType
) {
    
}
