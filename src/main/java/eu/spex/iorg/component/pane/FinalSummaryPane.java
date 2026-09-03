package eu.spex.iorg.component.pane;

import java.util.List;

import eu.spex.iorg.component.dialog.ConfirmationDialog;
import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.I18n;
import eu.spex.iorg.service.ImageService;
import eu.spex.iorg.service.Logger;
import eu.spex.iorg.voter.Voter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class FinalSummaryPane extends BorderPane {

    private static final int THUMBNAIL_SIZE = 170;

    private static final int CARD_WIDTH = THUMBNAIL_SIZE + 30;

    private final Mode mode;

    private final CategoryColors categoryColors;

    public FinalSummaryPane(Voter voter, CategoryColors categoryColors, EventHandler<ActionEvent> renameHandler) {
        this.mode = voter.getMode();
        this.categoryColors = categoryColors;
        List<FileVoteRecord> records = voter.generateFinalVoteResult();

        setStyle("-fx-background-color: #f5f5f7;");
        setTop(createHeader(mode, records.size()));
        setCenter(createGallery(records));
        setBottom(createButtonBar(renameHandler));
    }

    private VBox createHeader(Mode mode, int recordCount) {
        Label title = new Label(I18n.translate("label.summary"));
        title.setStyle("-fx-font-size: 1.6em; -fx-font-weight: bold;");

        Label count = new Label(recordCount + " " + I18n.translate("files"));
        count.setStyle("-fx-text-fill: #666666;");

        Label renameInfo = new Label(I18n.translate("mode." + mode.getParameter() + ".rename"));
        renameInfo.setWrapText(true);
        renameInfo.setStyle("-fx-text-fill: #666666;");

        HBox titleRow = new HBox(12, title, count);
        titleRow.setAlignment(Pos.BASELINE_LEFT);

        VBox header = new VBox(4, titleRow, renameInfo);
        header.setPadding(new Insets(18, 20, 14, 20));
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e0e0e0 transparent;"
                + " -fx-border-width: 0 0 1 0;");
        return header;
    }

    private ScrollPane createGallery(List<FileVoteRecord> records) {
        FlowPane grid = new FlowPane(16, 16);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.TOP_LEFT);
        for (FileVoteRecord record : records) {
            grid.getChildren().add(createCard(record));
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        // lets the grid span the whole window width, so the cards reflow with the window
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private VBox createCard(FileVoteRecord record) {
        StackPane imageBox = new StackPane(createThumbnail(record));
        imageBox.setMinHeight(THUMBNAIL_SIZE);
        imageBox.setPrefHeight(THUMBNAIL_SIZE);

        // a record can reach the summary without a rename, e.g. after an undo at the very end
        boolean rated = record.getFinalFileRename() != null;

        Label voting = new Label(rated ? record.getFinalVoting() : "-");
        voting.setStyle(badgeStyle(rated ? record.getFinalVoting() : null));
        StackPane.setAlignment(voting, Pos.TOP_RIGHT);
        imageBox.getChildren().add(voting);

        Label newName = new Label(rated ? record.getFinalNewFilePath() : record.getFileName());
        newName.setWrapText(true);
        newName.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;"
                + " -fx-text-fill: " + (rated ? "#1a1a1a" : "#909090") + ";");

        Label oldName = new Label(record.getFileName());
        oldName.setWrapText(true);
        oldName.setStyle("-fx-text-fill: #909090; -fx-font-size: 11px;");

        VBox card = new VBox(6, imageBox, newName, oldName);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + " -fx-border-color: #e2e2e5; -fx-border-width: 1; -fx-border-radius: 8;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);");
        return card;
    }

    /**
     * In categorize mode the badge carries the colour of its category, so the groups can be spotted
     * at a glance; the other modes keep one accent colour for the placement or rating.
     */
    private String badgeStyle(String voting) {
        String background = "#9e9e9e";
        String textColor = "#ffffff";
        if (voting != null) {
            if (mode == Mode.CATEGORIZE) {
                Color color = categoryColors.colorFor(voting);
                background = CategoryColors.toCss(color);
                textColor = CategoryColors.textColorOn(color);
            } else {
                background = "#e53935";
            }
        }
        // white ring plus a soft shadow, so the badge stays readable on top of any image
        return "-fx-background-color: #ffffff, " + background + ";"
                + " -fx-background-insets: 0, 2;"
                + " -fx-background-radius: 12, 10;"
                + " -fx-text-fill: " + textColor + ";"
                + " -fx-font-weight: bold; -fx-padding: 4 10;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 5, 0, 0, 1);";
    }

    private ImageView createThumbnail(FileVoteRecord record) {
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        try {
            Image image = ImageService.createThumbnailInMemory(record.getFilePath(), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            imageView.setImage(image);
        } catch (Exception ex) {
            Logger.error(ex, "Failed to load image: {0}", record.getFilePath());
        }
        return imageView;
    }

    private HBox createButtonBar(EventHandler<ActionEvent> renameHandler) {
        Button cancelButton = new Button(I18n.translate("button.cancel"));
        cancelButton.setOnAction(e -> quitAfterConfiguration());

        Button renameButton = new Button(I18n.translate("button.rename"));
        // deliberately not the default button: renaming is irreversible and Enter is a voting key,
        // so a stray Enter after the last vote must not trigger it
        renameButton.setOnAction(renameHandler);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(10, spacer, cancelButton, renameButton);
        buttonBar.setPadding(new Insets(12, 20, 12, 20));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0 transparent transparent transparent;"
                + " -fx-border-width: 1 0 0 0;");
        return buttonBar;
    }

    private void quitAfterConfiguration() {
        ConfirmationDialog dialog = new ConfirmationDialog("quit.confirm.question", "quit.confirm.yes", "quit.confirm.no");
        if (dialog.confirm().orElse(false)) {
            System.exit(0);
        }
    }
}
