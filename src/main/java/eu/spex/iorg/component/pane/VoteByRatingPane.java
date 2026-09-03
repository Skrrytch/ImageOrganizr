package eu.spex.iorg.component.pane;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.service.I18n;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class VoteByRatingPane extends VBox implements PreviewAwarePane {

    private static final Color SELECTED_COLOR = Color.web("#e53935");
    private static final Color HOVER_COLOR = Color.web("#fb8c00");
    private static final Color UNSELECTED_COLOR = Color.web("#bdbdbd");

    private static final int STAR_CELL_SIZE = 62;
    private static final double STAR_SCALE = 2.6;

    private final int maxRating;

    private final HBox starBar;

    private final List<SVGPath> stars = new ArrayList<>();

    private final PreviewPopup previewPopup = new PreviewPopup();

    private int selectedRating;

    private int hoverRating;

    private boolean voting;

    private Consumer<String> tagConsumer;

    private Function<String, VoteCheck> voteCheckFunction;

    public VoteByRatingPane(int maxRating) {
        this.maxRating = maxRating;
        this.starBar = createStarBar();

        Label hint = new Label(I18n.translate("mode.rate.hint"));
        hint.setStyle("-fx-text-fill: #808080; -fx-font-size: 0.9em;");

        setAlignment(Pos.CENTER);
        setSpacing(4);
        setPadding(new Insets(6, 10, 10, 10));
        getChildren().addAll(starBar, hint);
    }

    private HBox createStarBar() {
        HBox bar = new HBox(6);
        bar.setAlignment(Pos.CENTER);
        for (int rating = 1; rating <= maxRating; rating++) {
            bar.getChildren().add(createStarCell(rating));
        }
        return bar;
    }

    private Node createStarCell(int rating) {
        SVGPath star = new SVGPath();
        star.setContent("M10 1 L14 14 L1 6 L17 6 L4 14 Z");
        star.setFill(UNSELECTED_COLOR);
        star.setScaleX(STAR_SCALE);
        star.setScaleY(STAR_SCALE);
        stars.add(star);

        StackPane starBox = new StackPane(star);
        starBox.setMinSize(STAR_CELL_SIZE, STAR_CELL_SIZE);
        starBox.setPrefSize(STAR_CELL_SIZE, STAR_CELL_SIZE);
        // the whole cell reacts, not just the thin star outline
        starBox.setPickOnBounds(true);

        Label hotkey = new Label(hotkeyFor(rating));
        hotkey.setStyle("-fx-text-fill: #909090; -fx-font-size: 0.85em;");

        VBox cell = new VBox(starBox, hotkey);
        cell.setAlignment(Pos.CENTER);
        cell.setCursor(Cursor.HAND);
        cell.setPickOnBounds(true);
        cell.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> setHoverRating(rating));
        cell.addEventHandler(MouseEvent.MOUSE_EXITED, e -> setHoverRating(0));
        cell.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                showPreview(rating);
            } else if (e.getButton() == MouseButton.PRIMARY) {
                selectRating(rating);
            }
        });
        return cell;
    }

    private String hotkeyFor(int rating) {
        return rating == 10 ? "0" : String.valueOf(rating);
    }

    private void setHoverRating(int rating) {
        hoverRating = rating;
        fillStars();
    }

    private void fillStars() {
        for (int idx = 0; idx < stars.size(); idx++) {
            int rating = idx + 1;
            SVGPath star = stars.get(idx);
            if (selectedRating >= rating) {
                star.setFill(SELECTED_COLOR);
            } else if (hoverRating >= rating) {
                star.setFill(HOVER_COLOR);
            } else {
                star.setFill(UNSELECTED_COLOR);
            }
        }
    }

    /**
     * Votes for the given rating. Called by mouse click and by the keyboard shortcuts 1-9 and 0.
     */
    public void selectRating(int rating) {
        if (voting || rating < 1 || rating > maxRating || tagConsumer == null) {
            return;
        }
        voting = true;
        selectedRating = rating;
        hoverRating = 0;
        fillStars();
        previewPopup.hide();
        playStarPulse(rating, () -> {
            voting = false;
            tagConsumer.accept(getRatingTag(rating));
        });
    }

    /**
     * Lights up the selected stars one after the other, so a rating chosen by keyboard is confirmed
     * visually before the next image appears.
     */
    private void playStarPulse(int rating, Runnable onFinished) {
        ParallelTransition pulse = new ParallelTransition();
        for (int idx = 0; idx < rating; idx++) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(70), stars.get(idx));
            scale.setFromX(STAR_SCALE);
            scale.setFromY(STAR_SCALE);
            scale.setToX(STAR_SCALE * 1.35);
            scale.setToY(STAR_SCALE * 1.35);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.setDelay(Duration.millis(idx * 12L));
            pulse.getChildren().add(scale);
        }
        pulse.setOnFinished(e -> onFinished.run());
        pulse.play();
    }

    public int getMaxRating() {
        return maxRating;
    }

    public void setRecord(FileVoteRecord record, Consumer<String> tagConsumer, Function<String, VoteCheck> voteCheckFunction) {
        this.tagConsumer = tagConsumer;
        this.voteCheckFunction = voteCheckFunction;
        this.selectedRating = 0;
        this.hoverRating = 0;
        this.voting = false;
        fillStars();
    }

    private void showPreview(int rating) {
        if (voteCheckFunction == null) {
            return;
        }
        String ratingTag = getRatingTag(rating);
        VoteCheck voteCheck = voteCheckFunction.apply(ratingTag);
        List<FileVoteRecord> previewRecords = voteCheck == null ? null : voteCheck.getPreviewRecords();
        String title = previewRecords == null ? "" : I18n.translate(
                "mode." + Mode.RATE.getParameter() + ".preview.title", previewRecords.size(), ratingTag);
        previewPopup.show(starBar.getChildren().get(rating - 1), title, previewRecords);
    }

    private String getRatingTag(int rating) {
        return MessageFormat.format("{0,number,00}", rating);
    }

    @Override
    public void resetPreview() {
        previewPopup.hide();
    }
}
