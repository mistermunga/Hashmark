package xyz.ryansbeanfactory.hashmark.hashmark.service.signing;

import org.springframework.stereotype.Service;
import xyz.ryansbeanfactory.hashmark.hashmark.service.hashing.struct.MerkleNode;
import xyz.ryansbeanfactory.hashmark.hashmark.service.hashing.struct.MerkleTree;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageHasher {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Entry point for the signing pipeline.
     * Receives a canonical ARGB PNG and the exact chunk dimensions computed by
     * {@link ImageDivider}. Because the divider guarantees that both dimensions
     * evenly divide the image, every chunk is full-sized — no edge-case partials.
     *
     * @param image      canonical ARGB {@link BufferedImage} from the pre-processor
     * @param chunkDims  chunk width/height from {@link ImageDivider}
     * @return           a fully constructed {@link MerkleTree}
     */
    public MerkleTree hash(BufferedImage image, ImageDivider.ImageChunkDimensions chunkDims) {

        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        if (chunkDims == null) {
            throw new IllegalArgumentException("Chunk dimensions cannot be null.");
        }

        try {
            List<String> leafHashes = computeLeafHashes(image, chunkDims);
            return buildMerkleTree(leafHashes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm unavailable: " + HASH_ALGORITHM, e);
        }
    }

    /**
     * Iterates over the image in row-major order, hashing each chunk into a
     * leaf hash string. The chunk dimensions are exact — no partial chunks.
     */
    private List<String> computeLeafHashes(
            BufferedImage image,
            ImageDivider.ImageChunkDimensions chunkDims) throws NoSuchAlgorithmException {

        List<String> leafHashes = new ArrayList<>();

        int imageWidth  = image.getWidth();
        int imageHeight = image.getHeight();
        int chunkWidth  = chunkDims.width();
        int chunkHeight = chunkDims.height();

        for (int y = 0; y < imageHeight; y += chunkHeight) {
            for (int x = 0; x < imageWidth; x += chunkWidth) {
                BufferedImage chunk = image.getSubimage(x, y, chunkWidth, chunkHeight);
                byte[] chunkBytes   = chunkToBytes(chunk);
                String chunkHash    = hashBytes(chunkBytes);
                leafHashes.add(chunkHash);
            }
        }

        return leafHashes;
    }

    /**
     * Reads each pixel's ARGB channels into a flat byte array, scanning
     * top-to-bottom, left-to-right. Alpha is included because the pre-processor
     * canonicalizes to {@code TYPE_INT_ARGB} — omitting it would cause different
     * images with identical RGB but different alpha to produce the same hash.
     */
    private byte[] chunkToBytes(BufferedImage chunk) {

        int width  = chunk.getWidth();
        int height = chunk.getHeight();

        // 4 bytes per pixel (A, R, G, B)
        ByteArrayOutputStream baos = new ByteArrayOutputStream(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = chunk.getRGB(x, y);

                baos.write((argb >> 24) & 0xFF); // alpha
                baos.write((argb >> 16) & 0xFF); // red
                baos.write((argb >>  8) & 0xFF); // green
                baos.write( argb        & 0xFF); // blue
            }
        }

        return baos.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Merkle tree construction
    // -------------------------------------------------------------------------

    /**
     * Builds a Merkle tree bottom-up from a list of leaf hashes.
     * When a level has an odd number of nodes, the last node is duplicated so
     * that every parent always has two children — consistent with the prototype.
     */
    private MerkleTree buildMerkleTree(List<String> leafHashes) throws NoSuchAlgorithmException {

        if (leafHashes.isEmpty()) {
            throw new IllegalArgumentException("Cannot build a Merkle tree from an empty leaf list.");
        }

        // Seed the first level with leaf nodes
        List<MerkleNode> currentLevel = new ArrayList<>();
        for (int i = 0; i < leafHashes.size(); i++) {
            currentLevel.add(new MerkleNode(leafHashes.get(i), i));
        }

        // Reduce level-by-level until only the root remains
        while (currentLevel.size() > 1) {
            List<MerkleNode> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); i += 2) {
                MerkleNode left  = currentLevel.get(i);
                MerkleNode right = (i + 1 < currentLevel.size())
                        ? currentLevel.get(i + 1)
                        : left; // duplicate last node on odd-sized levels

                String combinedHash = hashString(left.getHash() + right.getHash());

                MerkleNode parent = new MerkleNode(combinedHash);
                parent.setLeft(left);
                parent.setRight(right);

                nextLevel.add(parent);
            }

            currentLevel = nextLevel;
        }

        return new MerkleTree(currentLevel.get(0), leafHashes);
    }

    // -------------------------------------------------------------------------
    // Hashing utilities
    // -------------------------------------------------------------------------

    private String hashBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        return bytesToHex(digest.digest(data));
    }

    private String hashString(String data) throws NoSuchAlgorithmException {
        return hashBytes(data.getBytes());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}