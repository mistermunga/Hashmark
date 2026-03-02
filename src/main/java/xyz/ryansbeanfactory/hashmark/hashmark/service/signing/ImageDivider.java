package xyz.ryansbeanfactory.hashmark.hashmark.service.signing;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class ImageDivider {

    private static final int MIN_CHUNK_SIZE = 64;
    private static final int MAX_CHUNKS = 15000;

    public ImageChunkDimensions divideImage(
            BufferedImage image,
            int heightFactor,
            int widthFactor) {

        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        if (heightFactor <= 0 || widthFactor <= 0) {
            throw new IllegalArgumentException("Factors must be positive.");
        }

        int height = image.getHeight();
        int width = image.getWidth();

        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException("Invalid image dimensions.");
        }

        if (width % widthFactor != 0) {
            throw new IllegalArgumentException("Invalid width factor.");
        }

        if (height % heightFactor != 0) {
            throw new IllegalArgumentException("Invalid height factor.");
        }

        int chunkWidth = width / widthFactor;
        int chunkHeight = height / heightFactor;

        if (chunkWidth < MIN_CHUNK_SIZE || chunkHeight < MIN_CHUNK_SIZE) {
            throw new IllegalArgumentException("Chunk dimensions too small.");
        }

        long chunkCount = (long) widthFactor * heightFactor;

        if (chunkCount > MAX_CHUNKS) {
            throw new IllegalArgumentException("Too many chunks.");
        }

        return new ImageChunkDimensions(
                chunkWidth,
                chunkHeight
        );
    }

    public List<Integer> getFactors(int dimension) {

        if (dimension <= 0) {
            throw new IllegalArgumentException("Invalid dimension.");
        }

        List<Integer> factors = new ArrayList<>();

        for (int i = 1; i * i <= dimension; i++) {
            if (dimension % i == 0) {
                factors.add(i);
                if (i != dimension / i) {
                    factors.add(dimension / i);
                }
            }
        }

        Collections.sort(factors);

        return factors;
    }

    public HashMap<String, List<Integer>> getFactors(BufferedImage image) {

        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        int height = image.getHeight();
        int width = image.getWidth();

        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException("Invalid image dimensions.");
        }

        HashMap<String, List<Integer>> factors = new HashMap<>();

        factors.put("widthFactors", getFactors(width));
        factors.put("heightFactors", getFactors(height));

        return factors;
    }

    public record ImageChunkDimensions(
            int width,
            int height
    ) {
    }
}