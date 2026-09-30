package eu.spex.iorg.component.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import eu.spex.iorg.component.pane.HeaderPane;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.I18n;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * The start dialog: one card per mode, the knockout card with a switch between its two variants.
 */
public class SelectModeDialog extends Dialog<ButtonType> {

    public static final ButtonType QUIT = new ButtonType(I18n.translate("mode.select.quit"), ButtonBar.ButtonData.CANCEL_CLOSE);
    public static final ButtonType START = new ButtonType(I18n.translate("mode.select.start"), ButtonBar.ButtonData.OK_DONE);

    private static final String ACCENT = "#2c58a0";

    private static final String CARD_STYLE = "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2;"
            + " -fx-cursor: hand;";

    private static final String CARD_NORMAL = CARD_STYLE + " -fx-background-color: white; -fx-border-color: #e2e2e5;";

    private static final String CARD_HOVER = CARD_STYLE + " -fx-background-color: white; -fx-border-color: #b8c4d9;";

    private static final String CARD_SELECTED = CARD_STYLE + " -fx-background-color: #f3f7fd; -fx-border-color: " + ACCENT + ";";

    private final int fileCount;

    private final List<ModeCard> cards = new ArrayList<>();

    private final ObjectProperty<ModeCard> selectedCard = new SimpleObjectProperty<>();

    public SelectModeDialog(File directory, int fileCount) {
        this.fileCount = fileCount;

        Label title = new Label(I18n.translate("mode.select.header"));
        title.setStyle("-fx-font-size: 1.6em; -fx-font-weight: bold;");
        Label subtitle = new Label(HeaderPane.displayPath(directory) + "  ·  " + fileCount + " " + I18n.translate("files"));
        subtitle.setStyle("-fx-text-fill: #666666;");
        VBox header = new VBox(4, title, subtitle);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            grid.getRowConstraints().add(row);
        }
        grid.add(createCard(Mode.ORDER, "mode.order", "mode.order.description", "mode.order.example", null), 0, 0);
        grid.add(createKnockoutCard(), 1, 0);
        grid.add(createCard(Mode.RATE, "mode.rate", "mode.rate.description", "mode.rate.example", null), 0, 1);
        grid.add(createCard(Mode.CATEGORIZE, "mode.categorize", "mode.categorize.description",
                "mode.categorize.example", null), 1, 1);

        Label hint = new Label(I18n.translate("mode.select.hint"));
        hint.setStyle("-fx-text-fill: #909090; -fx-font-size: 11px;");

        VBox content = new VBox(18, header, grid, hint);
        content.setPadding(new Insets(24, 24, 8, 24));
        content.setPrefWidth(780);

        setTitle(I18n.translate("mode.select.title"));
        setHeaderText(null);
        setGraphic(null);
        getDialogPane().setContent(content);
        getDialogPane().setStyle("-fx-background-color: #f5f5f7;");
        getDialogPane().getButtonTypes().addAll(QUIT, START);
        Node startButton = getDialogPane().lookupButton(START);
        startButton.setStyle("-fx-base: " + ACCENT + "; -fx-font-weight: bold; -fx-padding: 6 22;");
        startButton.disableProperty().bind(selectedCard.isNull());
    }

    private Node createKnockoutCard() {
        ToggleGroup variants = new ToggleGroup();
        ToggleButton simple = createVariantButton("mode.knockout.simple", variants, "-fx-background-radius: 14 0 0 14;");
        ToggleButton full = createVariantButton("mode.knockout.full", variants, "-fx-background-radius: 0 14 14 0;");
        simple.setSelected(true);
        // one variant always stays selected, like a segmented control
        variants.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });
        HBox switcher = new HBox(simple, full);

        ModeCard[] card = new ModeCard[1];
        Supplier<Mode> mode = () -> full.isSelected() ? Mode.FULL_KNOCKOUT : Mode.SIMPLE_KNOCKOUT;
        card[0] = createCard(mode, "mode.knockout", "mode.knockouts.description", "mode.knockouts.example", switcher);
        variants.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            select(card[0]);
            card[0].updateVotes();
        });
        return card[0];
    }

    private static ToggleButton createVariantButton(String key, ToggleGroup group, String radius) {
        ToggleButton button = new ToggleButton(I18n.translate(key));
        button.setToggleGroup(group);
        button.setFocusTraversable(false);
        String base = radius + " -fx-font-size: 11px; -fx-padding: 4 12; -fx-cursor: hand;";
        Runnable style = () -> button.setStyle(base + (button.isSelected()
                ? " -fx-background-color: " + ACCENT + "; -fx-text-fill: white;"
                : " -fx-background-color: #e6e9ef; -fx-text-fill: #333333;"));
        button.selectedProperty().addListener((observable, oldValue, newValue) -> style.run());
        style.run();
        return button;
    }

    private ModeCard createCard(Mode mode, String titleKey, String descriptionKey, String exampleKey, Node extra) {
        return createCard(() -> mode, titleKey, descriptionKey, exampleKey, extra);
    }

    private ModeCard createCard(Supplier<Mode> mode, String titleKey, String descriptionKey, String exampleKey,
                                Node extra) {
        ModeCard card = new ModeCard(mode, titleKey, descriptionKey, exampleKey, extra);
        card.setOnMouseEntered(e -> card.setStyle(selectedCard.get() == card ? CARD_SELECTED : CARD_HOVER));
        card.setOnMouseExited(e -> card.setStyle(selectedCard.get() == card ? CARD_SELECTED : CARD_NORMAL));
        card.setOnMouseClicked(e -> {
            select(card);
            if (e.getClickCount() == 2) {
                setResult(START);
                close();
            }
        });
        cards.add(card);
        return card;
    }

    private void select(ModeCard card) {
        selectedCard.set(card);
        cards.forEach(c -> c.setStyle(c == card ? CARD_SELECTED : CARD_NORMAL));
    }

    /** A rough estimate of the number of votes, so the user can judge the effort of a mode. */
    private int estimateVotes(Mode mode) {
        return switch (mode) {
            case SIMPLE_KNOCKOUT -> (fileCount % 2 == 0) ? fileCount - 1 : fileCount + 1;
            case FULL_KNOCKOUT -> (int) (fileCount * (fileCount - 1) / 2.0);
            case ORDER -> (int) (fileCount * Math.log(fileCount));
            case RATE, CATEGORIZE -> fileCount;
        };
    }

    public Mode getMode() {
        ModeCard card = selectedCard.get();
        return card == null ? null : card.mode.get();
    }

    private class ModeCard extends VBox {

        private final Supplier<Mode> mode;

        private final Label votes = new Label();

        ModeCard(Supplier<Mode> mode, String titleKey, String descriptionKey, String exampleKey, Node extra) {
            this.mode = mode;

            ImageView image = new ImageView(new Image(getClass().getResourceAsStream(
                    "/mode/" + mode.get().getParameter() + ".png")));
            image.setPreserveRatio(true);
            image.setFitWidth(64);
            image.setFitHeight(64);
            image.setSmooth(true);

            Label title = new Label(I18n.translate(titleKey));
            title.setStyle("-fx-font-size: 1.25em; -fx-font-weight: bold;");
            Label description = new Label(I18n.translate(descriptionKey));
            description.setWrapText(true);
            description.setStyle("-fx-text-fill: #444444;");
            description.setMinHeight(Region.USE_PREF_SIZE);

            VBox text = new VBox(6, title, description);
            if (extra != null) {
                text.getChildren().add(extra);
            }
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox top = new HBox(14, image, text);
            top.setAlignment(Pos.TOP_LEFT);

            Label example = new Label(I18n.translate(exampleKey));
            example.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #555555;"
                    + " -fx-background-color: #eef1f6; -fx-background-radius: 4; -fx-padding: 2 6;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            votes.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
            HBox bottom = new HBox(8, example, spacer, votes);
            bottom.setAlignment(Pos.CENTER_LEFT);

            Region fill = new Region();
            VBox.setVgrow(fill, Priority.ALWAYS);
            getChildren().addAll(top, fill, bottom);
            setSpacing(12);
            setPadding(new Insets(16));
            setMaxHeight(Double.MAX_VALUE);
            setStyle(CARD_NORMAL);
            updateVotes();
        }

        void updateVotes() {
            votes.setText(I18n.translate("mode.select.votes", estimateVotes(mode.get())));
        }
    }
}
