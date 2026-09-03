package eu.spex.iorg.component.pane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.Logger;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ImagePane extends VBox {

    private final Mode mode;

    private final ImageView imageView;

    private final Label imageName;

    private final Label keyHint;

    private final DropShadow highlight = new DropShadow(20, Color.web("#e53935"));

    public ImagePane(Mode mode) {
        setSpacing(10);
        this.mode = mode;

        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);

        // StackPane centers the image; pref 0 keeps it from claiming more than the parent grants
        StackPane imageViewPane = new StackPane(imageView);
        imageViewPane.setMinSize(0, 0);
        imageViewPane.setPrefSize(0, 0);

        this.keyHint = new Label();
        this.keyHint.setVisible(false);
        this.keyHint.setManaged(false);
        this.keyHint.setStyle("-fx-background-color: #eceff1; -fx-text-fill: #37474f; -fx-font-weight: bold;"
                + " -fx-padding: 1 8; -fx-background-radius: 10;");

        this.imageName = new Label();

        HBox nameRow = new HBox(8, keyHint, imageName);
        nameRow.setAlignment(Pos.CENTER);

        getChildren().addAll(nameRow, imageViewPane);
        VBox.setVgrow(imageViewPane, Priority.ALWAYS);
        VBox.setVgrow(nameRow, Priority.NEVER);

        imageView.fitWidthProperty().bind(imageViewPane.widthProperty());
        imageView.fitHeightProperty().bind(imageViewPane.heightProperty());

        if (isClickable()) {
            // the glow follows the image bounds, so the click target is unmistakable
            imageView.setCursor(Cursor.HAND);
            imageView.setOnMouseEntered(e -> imageView.setEffect(highlight));
            imageView.setOnMouseExited(e -> imageView.setEffect(null));
        }
    }

    /** In rate mode the rating is picked explicitly, so the image itself must not vote. */
    private boolean isClickable() {
        return mode != Mode.RATE;
    }

    /**
     * Shows which key selects this image, e.g. the arrow keys in the compare modes.
     */
    public void setKeyHint(String hint) {
        keyHint.setText(hint);
        keyHint.setVisible(hint != null);
        keyHint.setManaged(hint != null);
    }

    public void setHighlighted(boolean highlighted) {
        imageView.setEffect(highlighted ? highlight : null);
    }

    /**
     * Short pulse of this image. Returned unplayed so the caller can run both images together,
     * which is what confirms "the order stays as it is" in the compare modes.
     */
    public Animation createPulse() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(90), imageView);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.04);
        pulse.setToY(1.04);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        return pulse;
    }

    public void setRecord(FileVoteRecord record, Consumer<FileVoteRecord> eventConsumer) throws FileNotFoundException {
        if (record == null) {
            clearRecord();
        } else {
            InputStream stream = new FileInputStream(record.getFilePath());
            Image image;
            try {
                image = new Image(stream);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    Logger.warn("Could not close image stream for {0}: {1}", record.getFilePath(), e.getMessage());
                }
            }
            imageName.setText(record.getFileName());
            imageView.setImage(image);
            imageView.setEffect(null);
            if (isClickable()) {
                imageView.setOnMouseClicked((e) -> eventConsumer.accept(record));
            }
        }
    }

    public void clearRecord() {
        if (imageView != null) {
            imageView.setImage(null);
            imageView.setEffect(null);
        }
        if (imageName != null) {
            imageName.setText(null);
        }
    }
}
