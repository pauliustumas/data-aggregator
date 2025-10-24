package ai.foodscan.aggregate.db.service;

import ai.foodscan.aggregate.db.exception.BarcodeDecodingException;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Service
public class BarcodeService {

    private static final Logger logger = LoggerFactory.getLogger(BarcodeService.class);

    private final String errorImageLocation;

    public BarcodeService(@Value("${barcode.error.image.location}") String errorImageLocation) {
        this.errorImageLocation = errorImageLocation;
    }

    /**
     * Decodes a barcode from the given image bytes.
     *
     * @param imageBytes the image data as a byte array.
     * @return the decoded barcode as a String.
     */
    public Mono<String> decodeBarcodeFromImage(byte[] imageBytes) {
        try {
            InputStream is = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(is);
            if (bufferedImage == null) {
                // Store the image before throwing an exception.
                storeImage(imageBytes);
                throw new BarcodeDecodingException("Invalid image data.");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Optionally: Specify barcode formats if needed.
            Reader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            return Mono.just(result.getText());
        } catch (NotFoundException e) {
            storeImage(imageBytes);
            return Mono.error(new BarcodeDecodingException("No barcode found in the image.", e));
        } catch (Exception e) {
            storeImage(imageBytes);
            return Mono.error(new BarcodeDecodingException("Failed to decode barcode from image.", e));
        }
    }

    /**
     * Attempts to store the provided image bytes to the configured error image location.
     * The file is saved with a filename that includes the current timestamp and appropriate
     * extension (e.g., .png or .jpg) determined by the image's format.
     *
     * @param imageBytes the image data as a byte array.
     */
    private void storeImage(byte[] imageBytes) {
        try {
            Path errorDir = Paths.get(errorImageLocation);
            if (!Files.exists(errorDir)) {
                Files.createDirectories(errorDir);
            }

            // Detect the image format (default to png if detection fails)
            String format = getImageFormat(imageBytes);
            String fileName = "barcode_error_" + System.currentTimeMillis() + "." + format;
            Path filePath = errorDir.resolve(fileName);
            Files.write(filePath, imageBytes);
            logger.info("Stored error image at: {}", filePath.toAbsolutePath());
        } catch (Exception ex) {
            logger.error("Failed to store error image.", ex);
        }
    }

    /**
     * Attempts to detect the image format from the given image bytes.
     *
     * @param imageBytes the image data as a byte array.
     * @return a String representing the format (e.g., "png", "jpg"), or "png" if detection fails.
     */
    private String getImageFormat(byte[] imageBytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
             ImageInputStream iis = ImageIO.createImageInputStream(bais)) {
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
            if (imageReaders.hasNext()) {
                ImageReader reader = imageReaders.next();
                String formatName = reader.getFormatName().toLowerCase();
                reader.dispose();
                return formatName;
            }
        } catch (Exception ex) {
            logger.error("Error detecting image format.", ex);
        }
        // Default to png if the format cannot be determined.
        return "png";
    }
}
