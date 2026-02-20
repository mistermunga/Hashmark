package xyz.ryansbeanfactory.hashmark.hashmark.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class ImagePreProcessor {

    public BufferedImage preprocess(BufferedImage original) {}

    private BufferedImage canonicalizeToPNG(BufferedImage original) {
        BufferedImage canonical = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = canonical.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return canonical;
    }

    private BufferedImage save() {}
}
