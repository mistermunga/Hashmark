package xyz.ryansbeanfactory.hashmark.hashmark.service.signing;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.formats.png.PngImageParser;
import org.apache.commons.imaging.formats.png.PngImagingParameters;
import org.apache.commons.imaging.formats.png.AbstractPngText;
import org.springframework.stereotype.Service;
import xyz.ryansbeanfactory.hashmark.hashmark.entity.HashedImage;
import xyz.ryansbeanfactory.hashmark.hashmark.entity.User;
import xyz.ryansbeanfactory.hashmark.hashmark.repository.HashedImageRepository;
import xyz.ryansbeanfactory.hashmark.hashmark.service.hashing.struct.MerkleTree;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ImageSigner {

    // iTXt keyword namespace
    private static final String KEY_UUID         = "hashmark-uuid";
    private static final String KEY_ROOT_HASH    = "hashmark-root-hash";
    private static final String KEY_ALGORITHM    = "hashmark-algorithm";
    private static final String KEY_CHUNK_WIDTH  = "hashmark-chunk-width";
    private static final String KEY_CHUNK_HEIGHT = "hashmark-chunk-height";
    private static final String KEY_VERSION      = "hashmark-version";

    private static final String ALGORITHM_VALUE  = "SHA-256";
    private static final String VERSION_VALUE    = "1.0";

    private final HashedImageRepository hashedImageRepository;
    private final String                outputFolder;

    public ImageSigner(HashedImageRepository hashedImageRepository) {
        this.hashedImageRepository = hashedImageRepository;
        this.outputFolder          = Paths.get(System.getProperty("user.dir"), "output").toString();
    }

    // -------------------------------------------------------------------------
    // Result type returned to the orchestrator
    // -------------------------------------------------------------------------

    public record SigningResult(
            HashedImage entity,
            String      savedPath
    ) {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Signs an image by:
     * <ol>
     *   <li>Persisting a {@link HashedImage} record to obtain its DB-generated UUID.</li>
     *   <li>Embedding six Hashmark iTXt chunks into the PNG bytes.</li>
     *   <li>Writing the signed PNG to disk, named by UUID.</li>
     *   <li>Updating the entity's {@code imagePath} and saving again.</li>
     * </ol>
     */
    public SigningResult sign(
            BufferedImage image,
            MerkleTree tree,
            ImageDivider.ImageChunkDimensions chunkDims,
            String imageName,
            User signedBy) throws IOException, ImagingException {

        validateInputs(image, tree, chunkDims, imageName, signedBy);

        // Step 1 — persist partial record to get the DB-generated UUID
        HashedImage entity = new HashedImage();
        entity.setImageName(imageName);
        entity.setRootHash(tree.getRootHash());
        entity.setSignedBy(signedBy);
        entity = hashedImageRepository.save(entity);

        String uuid = entity.getImageID();

        // Step 2 — embed iTXt metadata into the PNG
        byte[] signedBytes = embedMetadata(image, uuid, tree, chunkDims);

        // Step 3 — write signed PNG to disk (UUID as filename to guarantee uniqueness)
        String savedPath = saveBytes(signedBytes, uuid);

        // Step 4 — update entity with resolved path
        entity.setImagePath(savedPath);
        entity = hashedImageRepository.save(entity);

        return new SigningResult(entity, savedPath);
    }

    // -------------------------------------------------------------------------
    // Metadata embedding
    // -------------------------------------------------------------------------

    /**
     * Encodes the image as PNG and injects six iTXt chunks via
     * {@link PngImageParser}. {@code PngText.Itxt} is used for UTF-8 support.
     * Language tag and translated keyword are empty — these are machine-readable
     * key-value pairs, not human-language text.
     */
    private byte[] embedMetadata(
            BufferedImage image,
            String uuid,
            MerkleTree tree,
            ImageDivider.ImageChunkDimensions chunkDims) throws IOException, ImagingException {

        List<AbstractPngText> textChunks = List.of(
                new AbstractPngText.Itxt(KEY_UUID,         uuid,                               "", ""),
                new AbstractPngText.Itxt(KEY_ROOT_HASH,    tree.getRootHash(),                 "", ""),
                new AbstractPngText.Itxt(KEY_ALGORITHM,    ALGORITHM_VALUE,                    "", ""),
                new AbstractPngText.Itxt(KEY_CHUNK_WIDTH,  String.valueOf(chunkDims.width()),  "", ""),
                new AbstractPngText.Itxt(KEY_CHUNK_HEIGHT, String.valueOf(chunkDims.height()), "", ""),
                new AbstractPngText.Itxt(KEY_VERSION,      VERSION_VALUE,                      "", "")
        );

        PngImagingParameters params = new PngImagingParameters();
        params.setTextChunks(textChunks);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new PngImageParser().writeImage(image, baos, params);

        return baos.toByteArray();
    }

    // -------------------------------------------------------------------------
    // File persistence
    // -------------------------------------------------------------------------

    /**
     * Writes the signed PNG byte array directly to disk without re-encoding.
     * Re-encoding via ImageIO would silently strip all custom iTXt chunks.
     */
    private String saveBytes(byte[] pngBytes, String imageName) throws IOException {

        Path outputPath = Paths.get(outputFolder);
        Files.createDirectories(outputPath);

        Path imagePath = outputPath.resolve(imageName + ".png");

        try (OutputStream os = Files.newOutputStream(imagePath)) {
            os.write(pngBytes);
        }

        if (!Files.exists(imagePath)) {
            throw new IOException("Failed to write signed image: " + imagePath);
        }

        return Paths.get(System.getProperty("user.dir"))
                .relativize(imagePath)
                .toString();
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateInputs(
            BufferedImage image,
            MerkleTree tree,
            ImageDivider.ImageChunkDimensions chunkDims,
            String imageName,
            User signedBy) {

        if (image == null)
            throw new IllegalArgumentException("Image cannot be null.");
        if (tree == null)
            throw new IllegalArgumentException("Merkle tree cannot be null.");
        if (tree.getRootHash() == null || tree.getRootHash().isBlank())
            throw new IllegalArgumentException("Root hash cannot be blank.");
        if (chunkDims == null)
            throw new IllegalArgumentException("Chunk dimensions cannot be null.");
        if (imageName == null || imageName.isBlank())
            throw new IllegalArgumentException("Image name cannot be blank.");
        if (signedBy == null)
            throw new IllegalArgumentException("Signing user cannot be null.");
    }
}