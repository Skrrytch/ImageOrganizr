package eu.spex.iorg.component.pane;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.service.I18n;
import eu.spex.iorg.service.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VoteByCategoryPanel extends VBox {

    private final ObservableList<String> tags;

    private final ListView<String> tagBoxLeft;

    private List<ImageView> imageViewList;

    private final TextField newTagInputField;

    private Label previewTitle;

    public VoteByCategoryPanel(List<String> predefinedTags) {

        tags = FXCollections.observableArrayList(predefinedTags);

        HBox tagBox = new HBox();
        tagBox.setSpacing(10);

        VBox tagBoxRight = new VBox();
        tagBoxRight.setPrefWidth(150);
        tagBoxRight.setSpacing(10);
        newTagInputField = new TextField();
        newTagInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addNewTag();
            }
        });
        Button addButton = new Button("Add Tag");
        addButton.setOnAction(event -> addNewTag());

        Label topDescription = new Label(I18n.translate("mode.categorize.taglist.description1"));
        topDescription.setWrapText(true);

        Label bottomDescription = new Label(I18n.translate("mode.categorize.taglist.description2"));
        bottomDescription.setWrapText(true);

        // Tag Controls and Informatiopn (TagBox: right)
        tagBoxRight.getChildren().addAll(topDescription, newTagInputField, addButton, bottomDescription);

        // Tag List (TagBox: left)
        tagBoxLeft = new ListView<>(tags);
        tagBoxLeft.setPrefWidth(150);
        tagBoxLeft.setStyle("-fx-font-size: 1.2em;");
        tagBox.getChildren().addAll(tagBoxLeft, tagBoxRight);

        GridPane imagePreviewBox = createPreviewImageBox();

        getChildren().addAll(imagePreviewBox, tagBox);

        VBox.setVgrow(imagePreviewBox, Priority.NEVER);
        VBox.setVgrow(tagBox, Priority.ALWAYS);

        setSpacing(10);
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
        final int columnCount = 2;

        previewTitle = new Label();
        previewTitle.setStyle("-fx-font-weight: bold");
        imageBox.add(previewTitle, 0, 0, columnCount, 1);

        int column = 0;
        int row = 1;
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
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(140);
        imageView.setFitHeight(210); // 2:3
        return imageView;
    }

    private void resetPreview(VoteCheck voteCheck, String category) {
        for (ImageView imageView : imageViewList) {
            imageView.setImage(null);
        }
        previewTitle.setText("");
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
        previewTitle.setText(I18n.translate("mode."+ Mode.CATEGORIZE.getParameter()+".preview.title", previewRecords.size(), category));
    }

    private void addNewTag() {
        String newTag = newTagInputField.getText();
        if (!newTag.isEmpty() && !tags.contains(newTag)) {
            tags.add(newTag);
            newTagInputField.clear();
        }
    }

    public void setRecord(FileVoteRecord record, Consumer<String> tagConsumer, Function<String, VoteCheck> voteCheckFunction) {
        tagBoxLeft.setOnMouseClicked(event -> {
            String selectedTag = tagBoxLeft.getSelectionModel().getSelectedItem();
            if (selectedTag != null) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    VoteCheck voteCheck = voteCheckFunction.apply(selectedTag);
                    resetPreview(voteCheck, selectedTag);
                } else if (event.getButton() == MouseButton.PRIMARY) {
                    tagConsumer.accept(selectedTag);
                    VoteCheck voteCheck = voteCheckFunction.apply(selectedTag);
                    resetPreview(voteCheck, selectedTag);
                }
            }
        });
    }

    public void clearRecord() {

    }
}
