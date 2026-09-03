package eu.spex.iorg.component.pane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.service.I18n;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class VoteByCategoryPane extends VBox implements PreviewAwarePane {

    /** Only the first nine categories can be reached by a number key. */
    public static final int MAX_HOTKEYS = 9;

    private final List<String> tags = new ArrayList<>();

    private final CategoryColors categoryColors;

    private final FlowPane chipBar;

    private final TextField newTagInput;

    private final PreviewPopup previewPopup = new PreviewPopup();

    private boolean voting;

    private Consumer<String> tagConsumer;

    private Function<String, VoteCheck> voteCheckFunction;

    public VoteByCategoryPane(List<String> predefinedTags, CategoryColors categoryColors) {
        this.categoryColors = categoryColors;
        this.chipBar = new FlowPane(8, 8);
        chipBar.setAlignment(Pos.CENTER);

        this.newTagInput = new TextField();
        newTagInput.setPromptText(I18n.translate("mode.categorize.newcategory"));
        newTagInput.setPrefColumnCount(14);
        // while this field has focus the number keys name a category instead of voting,
        // see isTypingNewCategory()
        newTagInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addNewTag();
            }
        });

        Button addButton = new Button("+");
        addButton.setStyle("-fx-font-weight: bold; -fx-padding: 4 12;");
        addButton.setFocusTraversable(false);
        addButton.setOnAction(event -> addNewTag());

        HBox addRow = new HBox(6, newTagInput, addButton);
        addRow.setAlignment(Pos.CENTER);

        Label hint = new Label(I18n.translate("mode.categorize.hint"));
        hint.setStyle("-fx-text-fill: #808080; -fx-font-size: 11px;");

        setAlignment(Pos.CENTER);
        setSpacing(6);
        setPadding(new Insets(6, 10, 10, 10));
        // can take the focus back from the input field via requestFocus(), but stays out of the
        // tab order so that Tab reaches the input field directly
        setFocusTraversable(false);
        getChildren().addAll(chipBar, addRow, hint);

        predefinedTags.forEach(this::addTag);
    }

    private void addNewTag() {
        String newTag = newTagInput.getText().trim();
        if (!newTag.isEmpty() && !tags.contains(newTag)) {
            addTag(newTag);
            newTagInput.clear();
        }
        requestFocus();
    }

    private void addTag(String tag) {
        tags.add(tag);
        chipBar.getChildren().add(createChip(tag, tags.size()));
    }

    private Node createChip(String tag, int position) {
        String hotkey = position <= MAX_HOTKEYS ? position + "  " : "";
        Button chip = new Button(hotkey + tag);

        Color color = categoryColors.colorFor(tag);
        String normalStyle = chipStyle(color);
        String hoverStyle = chipStyle(CategoryColors.hoverVariant(color));
        chip.setStyle(normalStyle);
        // chips must never take focus, otherwise they swallow the number keys
        chip.setFocusTraversable(false);
        chip.setOnMouseEntered(e -> chip.setStyle(hoverStyle));
        chip.setOnMouseExited(e -> chip.setStyle(normalStyle));
        chip.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                showPreview(tag);
            } else if (e.getButton() == MouseButton.PRIMARY) {
                selectCategory(tag);
            }
        });
        return chip;
    }

    private String chipStyle(Color color) {
        return "-fx-background-color: " + CategoryColors.toCss(color) + ";"
                + " -fx-text-fill: " + CategoryColors.textColorOn(color) + ";"
                + " -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 16; -fx-cursor: hand;";
    }

    /**
     * Votes for the category at the given position (1-based), used by the number keys.
     */
    public void selectCategoryByPosition(int position) {
        if (position >= 1 && position <= tags.size()) {
            selectCategory(tags.get(position - 1));
        }
    }

    public boolean isTypingNewCategory() {
        return newTagInput.isFocused();
    }

    private void selectCategory(String tag) {
        if (voting || tagConsumer == null) {
            return;
        }
        voting = true;
        previewPopup.hide();
        playChipPulse(tag, () -> {
            voting = false;
            tagConsumer.accept(tag);
        });
    }

    /**
     * Briefly enlarges the chosen chip, so a category picked by keyboard is confirmed visually
     * before the next image appears.
     */
    private void playChipPulse(String tag, Runnable onFinished) {
        int idx = tags.indexOf(tag);
        if (idx < 0 || idx >= chipBar.getChildren().size()) {
            onFinished.run();
            return;
        }
        ScaleTransition pulse = new ScaleTransition(Duration.millis(80), chipBar.getChildren().get(idx));
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.setOnFinished(e -> onFinished.run());
        pulse.play();
    }

    private void showPreview(String tag) {
        if (voteCheckFunction == null) {
            return;
        }
        int idx = tags.indexOf(tag);
        if (idx < 0) {
            return;
        }
        VoteCheck voteCheck = voteCheckFunction.apply(tag);
        List<FileVoteRecord> previewRecords = voteCheck == null ? null : voteCheck.getPreviewRecords();
        String title = previewRecords == null ? "" : I18n.translate(
                "mode." + Mode.CATEGORIZE.getParameter() + ".preview.title", previewRecords.size(), tag);
        previewPopup.show(chipBar.getChildren().get(idx), title, previewRecords);
    }

    public void setRecord(FileVoteRecord record, Consumer<String> tagConsumer, Function<String, VoteCheck> voteCheckFunction) {
        this.tagConsumer = tagConsumer;
        this.voteCheckFunction = voteCheckFunction;
        this.voting = false;
    }

    public void clearRecord() {
        previewPopup.hide();
    }

    @Override
    public void resetPreview() {
        previewPopup.hide();
    }
}
