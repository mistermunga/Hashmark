package xyz.ryansbeanfactory.hashmark.hashmark.service;

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

    public ImagePreProcessor() {
        ROOT_FOLDER = System.getProperty("user.dir");
        OUTPUT_FOLDER = Paths.get(ROOT_FOLDER, "output");
    }

    public BufferedImage canonicalizeToPNG(BufferedImage original) {
        BufferedImage canonical = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = canonical.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return canonical;
    }

    public String save(BufferedImage image, String imageName) throws IOException {
        Path imagePath = OUTPUT_FOLDER.resolve(imageName + ".png");
        Files.createDirectories(OUTPUT_FOLDER);
        ImageIO.write(image, "png", imagePath.toFile());
        return Paths.get(ROOT_FOLDER)
                .relativize(imagePath)
                .toString();
    }
}
