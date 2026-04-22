package com.asknehru.fruitsapi.service;

import com.asknehru.fruitsapi.exception.ApiValidationException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MediaStorageService {

    private static final Pattern DATA_URL_PATTERN =
        Pattern.compile("^data:image/([a-zA-Z0-9.+-]+);base64,(.+)$", Pattern.DOTALL);
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final Path mediaRoot;

    public MediaStorageService(@Value("${app.media-root:media}") String mediaRoot) {
        this.mediaRoot = Paths.get(mediaRoot).toAbsolutePath().normalize();
    }

    public String storeFruitDataImage(String dataUrl, String baseName) {
        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl == null ? "" : dataUrl.trim());
        if (!matcher.matches()) {
            throwValidation("images", "Only base64 image data URLs are supported.");
        }

        String mimeSubtype = matcher.group(1).toLowerCase(Locale.ROOT);
        if (!isSupportedSubtype(mimeSubtype)) {
            throwValidation("images", "Unsupported image type. Use jpg, png, webp, or gif.");
            return null;
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(matcher.group(2));
        } catch (IllegalArgumentException ex) {
            throwValidation("images", "Invalid base64 image payload.");
            return null;
        }

        if (bytes.length > MAX_IMAGE_BYTES) {
            throwValidation("images", "Image size must be less than or equal to 5 MB.");
            return null;
        }

        BufferedImage sourceImage = decodeImage(bytes);
        if (sourceImage == null) {
            throwValidation("images", "Invalid or unsupported image content.");
        }

        Path fruitsDir = mediaRoot.resolve("fruits");
        try {
            Files.createDirectories(fruitsDir);
            String safeName = slugify(baseName);
            String filename = safeName + "-" + UUID.randomUUID() + ".jpg";
            Path output = fruitsDir.resolve(filename).normalize();

            if (!output.startsWith(fruitsDir)) {
                throwValidation("images", "Invalid output file path.");
            }

            BufferedImage jpegImage = ensureRgbImage(sourceImage);
            try (OutputStream outputStream = Files.newOutputStream(output)) {
                boolean written = ImageIO.write(jpegImage, "jpg", outputStream);
                if (!written) {
                    throwValidation("images", "Failed to encode image as JPG.");
                }
            }

            return "/media/fruits/" + filename;
        } catch (IOException ex) {
            throwValidation("images", "Failed to save image to media folder.");
            return null;
        }
    }

    private boolean isSupportedSubtype(String subtype) {
        return switch (subtype) {
            case "jpeg", "jpg", "pjpeg", "png", "x-png", "webp", "gif" -> true;
            default -> false;
        };
    }

    private BufferedImage decodeImage(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(inputStream);
        } catch (IOException ex) {
            return null;
        }
    }

    private BufferedImage ensureRgbImage(BufferedImage input) {
        if (input.getType() == BufferedImage.TYPE_INT_RGB) {
            return input;
        }

        BufferedImage rgbImage = new BufferedImage(
            input.getWidth(),
            input.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = rgbImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(input, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return rgbImage;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "image" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "image" : normalized;
    }

    private void throwValidation(String field, String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(field, List.of(message));
        throw new ApiValidationException(errors);
    }
}
