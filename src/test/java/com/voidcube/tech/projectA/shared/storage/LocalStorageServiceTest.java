package com.voidcube.tech.projectA.shared.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void deveSalvarImagemComNomeUnico()
            throws IOException {
        LocalStorageService service =
                new LocalStorageService(
                        temporaryDirectory.toString()
                );

        MockMultipartFile file =
                createPngFile(
                        "produto.png",
                        800,
                        600
                );

        String savedPath = service.save(file);

        Resource resource =
                service.fetch(savedPath);

        assertNotEquals(
                "produto.png",
                savedPath
        );

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
void deveRedimensionarImagemMuitoGrande()
        throws IOException {
    LocalStorageService service =
            new LocalStorageService(
                    temporaryDirectory.toString()
            );

    MockMultipartFile file =
            createPngFile(
                    "produto-grande.png",
                    2400,
                    1200
            );

    String savedPath = service.save(file);

    Resource resource =
            service.fetch(savedPath);

    try (
            InputStream inputStream =
                    resource.getInputStream()
    ) {
        BufferedImage savedImage =
                ImageIO.read(inputStream);

        assertEquals(
                1200,
                savedImage.getWidth()
        );

        assertEquals(
                600,
                savedImage.getHeight()
        );
    }
}
   

    @Test
    void deveRecusarArquivoQueNaoEImagem() {
        LocalStorageService service =
                new LocalStorageService(
                        temporaryDirectory.toString()
                );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "arquivo.png",
                        "image/png",
                        "isso nao e uma imagem".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.save(file)
        );
    }

    @Test
    void deveRecusarArquivoMaiorQueCincoMb() {
        LocalStorageService service =
                new LocalStorageService(
                        temporaryDirectory.toString()
                );

        byte[] oversizedContent =
                new byte[
                    (5 * 1024 * 1024) + 1
                ];

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "grande.png",
                        "image/png",
                        oversizedContent
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.save(file)
        );
    }

    private MockMultipartFile createPngFile(
            String filename,
            int width,
            int height
    ) throws IOException {
        BufferedImage image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_ARGB
                );

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        ImageIO.write(
                image,
                "png",
                output
        );

        return new MockMultipartFile(
                "file",
                filename,
                "image/png",
                output.toByteArray()
        );
    }
}
