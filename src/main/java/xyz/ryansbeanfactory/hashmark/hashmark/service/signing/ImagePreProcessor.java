package xyz.ryansbeanfactory.hashmark.hashmark.service.signing;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImagePreProcessor {

    private static Path OUTPUT_FOLDER;
    private final String ROOT_FOLDER;

    private static final int MIN_DIMENSION = 64;
    private static final int MAX_DIMENSION = 8000;
    private static final long MAX_PIXELS = 64_000_000L;

    public ImagePreProcessor() {
        ROOT_FOLDER = System.getProperty("user.dir");
        OUTPUT_FOLDER = Paths.get(ROOT_FOLDER, "output");
    }

    public BufferedImage canonicalizeToPNG(BufferedImage original) {

        if (original == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid image dimensions.");
        }

        if (width < MIN_DIMENSION || height < MIN_DIMENSION) {
            throw new IllegalArgumentException("Image dimensions too small.");
        }

        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            throw new IllegalArgumentException("Image dimensions too large.");
        }

        long pixels = (long) width * height;

        if (pixels > MAX_PIXELS) {
            throw new IllegalArgumentException("Image pixel count too large.");
        }

        BufferedImage canonical = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = canonical.createGraphics();

        if (g2d == null) {
            throw new IllegalStateException("Graphics context creation failed.");
        }

        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return canonical;
    }

    public String save(BufferedImage image, String imageName) throws IOException {

        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        if (imageName == null || imageName.isBlank()) {
            throw new IllegalArgumentException("Invalid image name.");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid image dimensions.");
        }

        Files.createDirectories(OUTPUT_FOLDER);

        Path imagePath = OUTPUT_FOLDER.resolve(imageName + ".png");

        ImageIO.write(image, "png", imagePath.toFile());

        if (!Files.exists(imagePath)) {
            throw new IOException("Failed to write image.");
        }

        return Paths.get(ROOT_FOLDER)
                .relativize(imagePath)
                .toString();
    }
}