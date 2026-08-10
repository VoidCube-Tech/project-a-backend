package com.voidcube.tech.projectA.shared.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;


public interface ImageStorageService {
    
    String save(MultipartFile file);

    Resource fetch(String path);

    void delete(String path);
}
