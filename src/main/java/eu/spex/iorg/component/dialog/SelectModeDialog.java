package eu.spex.iorg.component.dialog;

import java.util.HashMap;
import java.util.Map;

import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SelectModeDialog extends Dialog<ButtonType> {

    public static final ButtonType QUIT = new ButtonType(I18n.translate("mode.select.quit"));
    public static final ButtonType START = new ButtonType(I18n.translate("mode.select.start"));

    private final ToggleGroup modeSelectionGroup;

    private final Map<RadioButton, Mode> radioButtonMap;

    private final int fileCount;
    private Label fileVotesLabel;

    public SelectModeDialog(int fileCount) {
        this.fileCount = fileCount;

        VBox contentBox = new VBox();
        contentBox.setPrefWidth(700);

        contentBox.setPadding(new Insets(20));
        contentBox.setSpacing(10);

        VBox contantContainer = new VBox();
        contantContainer.setPadding(new Insets(20));
        contantContainer.setSpacing(10);

        // Create a toggle group for the radio buttons
        modeSelectionGroup = new ToggleGroup();
        radioButtonMap = new HashMap<>();

        addTournamentModes(contantContainer);
        addSeparator(contantContainer);
        addMode(Mode.ORDER, contantContainer);
        addSeparator(contantContainer);
        addMode(Mode.CATEGORIZE, contantContainer);
        addSeparator(contantContainer);
        addMode(Mode.RATE, contantContainer);

        modeSelectionGroup.selectedToggleProperty().addListener(
                (observable, oldButton, newButton) -> updateSelection(newButton));

        addSeparator(contantContainer);
        addFileInfo(fileCount, contantContainer);

        contentBox.getChildren().add(contantContainer);

        setTitle(I18n.translate("mode.select.title"));
        setHeaderText(null);
        setGraphic(null);
        getDialogPane().setContent(contentBox);

        getDialogPane().getButtonTypes().addAll(QUIT, START);
    }


    private void addTournamentModes(VBox radioBox) {
        Mode mode = Mode.SIMPLE_KNOCKOUT;
        ImageView imageView = createModeImagePane(mode);

        VBox vbox = new VBox();
        vbox.setSpacing(5);

        RadioButton radioButton1 = createRadioButton(Mode.SIMPLE_KNOCKOUT);
        RadioButton radioButton2 = createRadioButton(Mode.FULL_KNOCKOUT);
        String description = I18n.translate("mode.knockouts.description");
        String renameInfo = I18n.translate("mode.knockouts.rename");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-font-weight: bold;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(0, 0, 0, 28));

        Label renameLabel = new Label(renameInfo);
        renameLabel.setWrapText(true);
        renameLabel.setPadding(new Insets(0, 0, 0, 28));

        vbox.getChildren().addAll(radioButton1, radioButton2, descriptionLabel, renameLabel);

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.getChildren().addAll(vbox, imageView);

        hbox.setOnMouseClicked((e) -> {
            if (!radioButton2.isSelected()) {
                radioButton1.setSelected(true);
            }
            if (e.getClickCount() == 2) {
                this.setResult(START);
                this.close();
            }
        });

        radioBox.getChildren().addAll(hbox);
    }


    private ImageView createModeImagePane(Mode mode) {
        Image image = new Image(getClass().getResourceAsStream("/mode/" + mode.getParameter() + ".png"));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(100);
        return imageView;
    }

    private RadioButton createRadioButton(Mode mode) {
        String name = I18n.translate("mode." + mode.getParameter());
        RadioButton radioButton1 = new RadioButton(name);
        radioButton1.setStyle("-fx-font-weight: bold;-fx-font-size: 1.2em");
        radioButton1.setToggleGroup(modeSelectionGroup);
        radioButtonMap.put(radioButton1, mode);
        return radioButton1;
    }


    private void addMode(Mode mode, VBox container) {
        ImageView imageView = createModeImagePane(mode);

        VBox vbox = new VBox();
        vbox.setSpacing(5);

        RadioButton radioButton = createRadioButton(mode);
        String description = I18n.translate("mode." + mode.getParameter() + ".description");
        String renameInfo = I18n.translate("mode." + mode.getParameter() + ".rename");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-font-weight: bold;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPadding(new Insets(0, 0, 0, 28));

        Label renameLabel = new Label(renameInfo);
        renameLabel.setWrapText(true);
        renameLabel.setPadding(new Insets(0, 0, 0, 28));

        vbox.getChildren().addAll(radioButton, descriptionLabel, renameLabel);

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.getChildren().addAll(vbox, imageView);
        HBox.setHgrow(vbox, Priority.ALWAYS);

        hbox.setOnMouseClicked((e) -> {
            radioButton.setSelected(true);
            if (e.getClickCount() == 2) {
                this.setResult(START);
                this.close();
            }
        });

        container.getChildren().addAll(hbox);
    }


    private void updateSelection(Toggle newButton) {
        Mode mode = getMode(newButton);
        if (mode != null) {
            int estimatedComparisons = switch (mode) {
                case SIMPLE_KNOCKOUT -> (fileCount % 2 == 0) ? fileCount - 1 : fileCount + 1;
                case FULL_KNOCKOUT -> (int) (fileCount * (fileCount - 1) / 2.0);
                case ORDER -> (int) (fileCount * Math.log(fileCount));
                case RATE -> fileCount;
                case CATEGORIZE -> fileCount;
            };
            this.fileVotesLabel.setText(estimatedComparisons + " " + I18n.translate("estimated.votings"));
        }
    }


    private void addFileInfo(int fileCount, VBox container) {
        Label fileCountLabel = new Label();
        fileCountLabel.setText(fileCount + " " + I18n.translate("files"));

        fileVotesLabel = new Label();
        HBox box = new HBox(fileCountLabel, fileVotesLabel);
        box.setSpacing(20);
        HBox.setHgrow(box, Priority.ALWAYS);
        HBox.setHgrow(fileCountLabel, Priority.ALWAYS);
        HBox.setHgrow(fileVotesLabel, Priority.ALWAYS);
        fileCountLabel.setAlignment(Pos.CENTER_LEFT);
        fileVotesLabel.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().add(box);
    }

    private void addSeparator(VBox radioBox) {
        radioBox.getChildren().add(new Separator());
    }

    public Mode getMode(Toggle toggle) {
        return radioButtonMap.get(toggle);
    }

    public Mode getMode() {
        Toggle selectedToggle = modeSelectionGroup.getSelectedToggle();
        return getMode(selectedToggle);
    }
}
