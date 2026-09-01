package com.voidcube.tech.projectA.shared.storage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.voidcube.tech.projectA.shared.exception.ImageStorageException;
import com.voidcube.tech.projectA.shared.exception.InvalidImageException;

@Service
public class LocalStorageService implements ImageStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;
    private static final long MAX_PIXELS = 40_000_000L;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final Path rootDirectory;

    public LocalStorageService(@Value("${app.storage.images.directory}") String directory) {
        this.rootDirectory = Paths.get(directory).toAbsolutePath().normalize();
        initializeDirectory();
    }

    @Override
    public String save(MultipartFile file) {
        validateBasicProperties(file);

        DecodedImage decodedImage = decodeAndValidate(file);
        BufferedImage normalizedImage = resizeIfNecessary(
                decodedImage.image(),
                decodedImage.format()
        );

        String filename = UUID.randomUUID() + "." + decodedImage.extension();
        Path destination = rootDirectory.resolve(filename).normalize();

        ensureInsideRootDirectory(destination);
        writeImage(normalizedImage, decodedImage.format(), destination);

        return filename;
    }

    @Override
    public Resource fetch(String path) {
        validateStoredPath(path);

        Path resolvedPath = rootDirectory.resolve(path).normalize();
        ensureInsideRootDirectory(resolvedPath);

        try {
            Resource resource = new UrlResource(resolvedPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ImageStorageException("O arquivo da imagem não foi encontrado.");
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new ImageStorageException("Não foi possível carregar a imagem.", exception);
        }
    }

    @Override
    public void delete(String path) {
        validateStoredPath(path);

        Path resolvedPath = rootDirectory.resolve(path).normalize();
        ensureInsideRootDirectory(resolvedPath);

        try {
            Files.deleteIfExists(resolvedPath);
        } catch (IOException exception) {
            throw new ImageStorageException("Não foi possível remover a imagem.", exception);
        }
    }

    private void initializeDirectory() {
        try {
            Files.createDirectories(rootDirectory);
        } catch (IOException exception) {
            throw new ImageStorageException("Não foi possível criar a pasta de imagens.", exception);
        }
    }

    private void validateStoredPath(String path) {
        if (path == null || path.isBlank()) {
            throw new ImageStorageException("O caminho da imagem não pode estar vazio.");
        }
    }

    private void validateBasicProperties(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("A imagem não pode estar vazia.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageException("A imagem deve possuir no máximo 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidImageException(
                    "Formato não permitido. Utilize JPEG, PNG ou WEBP."
            );
        }
    }

    private DecodedImage decodeAndValidate(MultipartFile file) {
        try (
                InputStream inputStream = file.getInputStream();
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)
        ) {
            if (imageInputStream == null) {
                throw new InvalidImageException("O arquivo enviado não é uma imagem válida.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);

            if (!readers.hasNext()) {
                throw new InvalidImageException("O arquivo enviado não é uma imagem válida.");
            }

            return readImage(file, imageInputStream, readers.next());
        } catch (IOException exception) {
            throw new InvalidImageException("Não foi possível ler a imagem.", exception);
        }
    }

    private DecodedImage readImage(
            MultipartFile file,
            ImageInputStream imageInputStream,
            ImageReader reader
    ) throws IOException {
        try {
            reader.setInput(imageInputStream, true, true);

            String format = normalizeFormat(reader.getFormatName());
            validateRealFormat(file.getContentType(), format);

            int width = reader.getWidth(0);
            int height = reader.getHeight(0);

            validateDimensions(width, height);

            BufferedImage image = reader.read(0);

            if (image == null) {
                throw new InvalidImageException("Não foi possível decodificar a imagem.");
            }

            return new DecodedImage(image, format, extensionFor(format));
        } finally {
            reader.dispose();
        }
    }

    private String normalizeFormat(String format) {
        String normalized = format.toLowerCase(Locale.ROOT);

        if (normalized.equals("jpg") || normalized.equals("jpeg")) {
            return "jpeg";
        }

        if (normalized.equals("png") || normalized.equals("webp")) {
            return normalized;
        }

        throw new InvalidImageException("Formato real da imagem não permitido.");
    }

    private String extensionFor(String format) {
        return switch (format) {
            case "jpeg" -> "jpg";
            case "png" -> "png";
            case "webp" -> "webp";
            default -> throw new InvalidImageException("Formato de imagem não permitido.");
        };
    }

    private void validateRealFormat(String declaredContentType, String realFormat) {
        String contentType = declaredContentType.toLowerCase(Locale.ROOT);

        boolean validJpeg = realFormat.equals("jpeg")
                && (contentType.equals("image/jpeg") || contentType.equals("image/jpg"));

        boolean validPng = realFormat.equals("png") && contentType.equals("image/png");
        boolean validWebp = realFormat.equals("webp") && contentType.equals("image/webp");

        if (!validJpeg && !validPng && !validWebp) {
            throw new InvalidImageException(
                    "O conteúdo do arquivo não corresponde ao tipo informado."
            );
        }
    }

    private void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new InvalidImageException("Dimensões de imagem inválidas.");
        }

        if ((long) width * height > MAX_PIXELS) {
            throw new InvalidImageException("A imagem possui dimensões excessivas.");
        }
    }

    private BufferedImage resizeIfNecessary(BufferedImage original, String format) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double scale = Math.min(
                1.0,
                Math.min(
                        (double) MAX_WIDTH / originalWidth,
                        (double) MAX_HEIGHT / originalHeight
                )
        );

        if (scale == 1.0) {
            return original;
        }

        int newWidth = Math.max(1, (int) Math.round(originalWidth * scale));
        int newHeight = Math.max(1, (int) Math.round(originalHeight * scale));
        boolean supportsTransparency = format.equals("png") || format.equals("webp");

        int imageType = supportsTransparency
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;

        BufferedImage resized = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D graphics = resized.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            graphics.drawImage(original, 0, 0, newWidth, newHeight, null);
        } finally {
            graphics.dispose();
        }

        return resized;
    }

    private void writeImage(BufferedImage image, String format, Path destination) {
        Path temporaryFile = null;

        try {
            temporaryFile = Files.createTempFile(rootDirectory, "image-", ".tmp");

            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                if (!ImageIO.write(image, format, output)) {
                    throw new ImageStorageException(
                            "Não existe codificador para o formato da imagem."
                    );
                }
            }

            moveTemporaryFile(temporaryFile, destination);
        } catch (IOException exception) {
            throw new ImageStorageException("Não foi possível salvar a imagem.", exception);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    private void moveTemporaryFile(Path temporaryFile, Path destination) throws IOException {
        try {
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, destination);
        }
    }

    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // A falha principal já foi tratada. Este arquivo temporário poderá ser limpo depois.
        }
    }

    private void ensureInsideRootDirectory(Path path) {
        if (!path.startsWith(rootDirectory)) {
            throw new ImageStorageException("Caminho de imagem inválido.");
        }
    }

    private record DecodedImage(
            BufferedImage image,
            String format,
            String extension
    ) {
    }
}