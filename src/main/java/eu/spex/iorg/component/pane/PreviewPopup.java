package eu.spex.iorg.component.pane;

import java.io.File;
import java.util.List;

import eu.spex.iorg.model.FileVoteRecord;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

/**
 * Floating preview of the images already assigned to a rating or category. Replaces the permanently
 * reserved preview grid, so the image itself can use the space.
 */
public class PreviewPopup {

    private static final int IMAGE_SIZE = 150;

    private static final int MAX_IMAGES = 4;

    private final Popup popup = new Popup();

    private final Label title = new Label();

    private final HBox images = new HBox(8);

    public PreviewPopup() {
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        images.setAlignment(Pos.CENTER);

        VBox content = new VBox(8, title, images);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white;"
                + " -fx-border-color: #9e9e9e; -fx-border-width: 1;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 3);");

        popup.getContent().add(content);
        popup.setAutoHide(true);
    }

    /**
     * Shows the newest records above the given anchor. Hides the popup when there is nothing to show.
     */
    public void show(Node anchor, String titleText, List<FileVoteRecord> records) {
        if (records == null || records.isEmpty()) {
            hide();
            return;
        }

        images.getChildren().clear();
        for (int idx = records.size() - 1; idx >= 0 && images.getChildren().size() < MAX_IMAGES; idx--) {
            images.getChildren().add(createImageView(records.get(idx)));
        }
        title.setText(titleText);

        Bounds anchorBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (anchorBounds == null) {
            return;
        }
        // show first, then correct the position - width and height are only known once it is on screen
        popup.show(anchor.getScene().getWindow());
        popup.setX(anchorBounds.getCenterX() - popup.getWidth() / 2);
        popup.setY(anchorBounds.getMinY() - popup.getHeight() - 8);
    }

    public void hide() {
        popup.hide();
    }

    private ImageView createImageView(FileVoteRecord record) {
        // the URI form also copes with spaces and special characters in the path
        String url = new File(record.getFilePath()).toURI().toString();
        ImageView imageView = new ImageView(new Image(url, IMAGE_SIZE, IMAGE_SIZE, true, true));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        return imageView;
    }
}
