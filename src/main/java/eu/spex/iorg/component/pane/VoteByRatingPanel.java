package eu.spex.iorg.component.pane;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.service.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class VoteByRatingPanel extends VBox {

    private static final Color HIGHLIGHT_COLOR = Color.RED;
    private static final Color PREVIEWED_COLOR = Color.ORANGE;
    private static final Color UNSELECTED_COLOR = Color.GRAY;

    private int previewRating = 0;

    private int selectedRating = 0;
    private List<ImageView> imageViewList;

    private final HBox ratingStarsBox;

    public VoteByRatingPanel(int maxRating) {
        setPadding(new Insets(10));
        setSpacing(20);

        this.ratingStarsBox = createRatingStars(maxRating);
        GridPane previewImageBox = createPreviewImageBox();

        getChildren().addAll(
                ratingStarsBox,
                new Separator(),
                previewImageBox);

        VBox.setVgrow(ratingStarsBox, Priority.NEVER);
        VBox.setVgrow(previewImageBox, Priority.NEVER);
    }

    private HBox createRatingStars(int maxRating) {
        var ratingStarsBox = new HBox(10);
        ratingStarsBox.setAlignment(Pos.CENTER);
        for (int rating = 1; rating <= maxRating; rating++) {
            var starBox = createStar(rating);
            int ratingValue = rating;
            starBox.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> fillStars(ratingValue));
            starBox.addEventHandler(MouseEvent.MOUSE_EXITED, e -> fillStars());
            ratingStarsBox.getChildren().add(starBox);
        }
        return ratingStarsBox;
    }

    private void fillStars() {
        fillStars(0);
    }
    private void fillStars(int highlightRating) {
        int rating = 1;
        for (Node starBox : ratingStarsBox.getChildren()) {
            Pane starBoxPane = (Pane) starBox;
            SVGPath star = (SVGPath) starBoxPane.getChildren().get(0);
            if (highlightRating >= rating || selectedRating >= rating) {
                star.setFill(HIGHLIGHT_COLOR);
            } else if (previewRating >= rating) {
                star.setFill(PREVIEWED_COLOR);
            } else {
                star.setFill(UNSELECTED_COLOR);
            }
            rating++;
        }
    }

    private Pane createStar(int starNumber) {
        SVGPath star = new SVGPath();
        star.setContent("M10 1 L14 14 L1 6 L17 6 L4 14 Z");
        star.setFill(UNSELECTED_COLOR);
        star.setScaleX(1.75);
        star.setScaleY(1.75);

        return new HBox(star);
    }

    private void setPreviewRating(int rating) {
        previewRating = rating;
        fillStars();
    }

    private void setSelectedRating(int rating) {
        selectedRating = rating;
        previewRating = selectedRating;
        fillStars();
    }

    public void setRecord(FileVoteRecord record, Consumer<String> tagConsumer, Function<String, VoteCheck> voteCheckFunction) {
        selectedRating = 0;
        int rating = 1;
        for (Node child : ratingStarsBox.getChildren()) {
            int currentRating = rating;
            child.setOnMouseClicked((e) -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    String ratingTag = getRatingTag(previewRating);
                    setPreviewRating(currentRating);
                    VoteCheck voteCheck = voteCheckFunction.apply(ratingTag);
                    resetPreview(voteCheck);
                } else if (e.getButton() == MouseButton.PRIMARY) {
                    setSelectedRating(currentRating);
                    String ratingTag = getRatingTag(selectedRating);
                    tagConsumer.accept(ratingTag);
                    VoteCheck voteCheck = voteCheckFunction.apply(ratingTag);
                    resetPreview(voteCheck);
                }
            });
            rating++;
        }
        fillStars();
    }

    private GridPane createPreviewImageBox() {
        GridPane imageBox = new GridPane();
        imageBox.setHgap(5);
        imageBox.setVgap(5);
        imageViewList = new ArrayList<>();
        imageViewList.add(createNewPreviewImageView());
        imageViewList.add(createNewPreviewImageView());
        imageViewList.add(createNewPreviewImageView());
        imageViewList.add(createNewPreviewImageView());
        int columnCount = 2;
        int column = 0;
        int row = 0;
        for (ImageView imageView : imageViewList) {
            imageBox.add(imageView, column, row);
            column++;
            if (column >= columnCount) {
                column = 0;
                row++;
            }
        }
        return imageBox;
    }

    private ImageView createNewPreviewImageView() {
        ImageView imageView = new ImageView();
        imageView.setStyle("-fx-border-width: 1;-fx-border-color: black;");
        imageView.setFitWidth(140);
        imageView.setFitHeight(210); // 2:3
        return imageView;
    }

    private void resetPreview(VoteCheck voteCheck) {
        for (ImageView imageView : imageViewList) {
            imageView.setImage(null);
        }
        if (voteCheck == null) {
            return;
        }
        List<FileVoteRecord> previewRecords = voteCheck.getPreviewRecords();
        if (previewRecords == null) {
            return;
        }
        int previewIdx = previewRecords.size() - 1;
        int viewIdx = 0;
        while (previewIdx >= 0 && viewIdx < imageViewList.size()) {
            FileVoteRecord record = previewRecords.get(previewIdx);
            try {
                InputStream stream = new FileInputStream(record.getFilePath());
                Image image = new Image(stream);
                imageViewList.get(viewIdx).setImage(image);
            } catch (Exception ex) {
                Logger.error(ex, "Failed to load image: " + record.getFilePath());
            }
            previewIdx--;
            viewIdx++;
        }
    }


    private String getRatingTag(int rating) {
        return MessageFormat.format("{0,number,00}", rating);
    }
}
