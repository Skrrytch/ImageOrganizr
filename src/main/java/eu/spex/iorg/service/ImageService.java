package eu.spex.iorg.service;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ImageService {

    public static Image createThumbnailInMemory(String inputImagePath, int thumbnailWidth, int thumbnailHeight) {
        Image originalImage = new Image("file:" + inputImagePath);
        double width = originalImage.getWidth();
        double height = originalImage.getHeight();
        if (thumbnailWidth>=width && thumbnailHeight>=height) {
            return originalImage;
        }
        // empty canvas with the requested thumbnail size
        Canvas canvas = new Canvas(thumbnailWidth, thumbnailHeight);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        double ratio = Math.min(thumbnailWidth/width, thumbnailHeight/height);

        double targetHeight = height * ratio;
        double targetWidth = width * ratio;

        // draw the original image scaled down to the thumbnail size
        graphicsContext.drawImage(originalImage, 0, 0, targetWidth, targetHeight);

        // take the scaled image from the canvas
        return canvas.snapshot(null, null);
    }
}
