package eu.spex.iorg.component.pane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.spex.iorg.component.dialog.ConfirmationDialog;
import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.service.I18n;
import eu.spex.iorg.service.ImageService;
import eu.spex.iorg.service.Logger;
import eu.spex.iorg.voter.Voter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class FinalSummaryPane extends BorderPane {

    public Map<String, ObjectProperty> imageCache = new HashMap<>();

    public FinalSummaryPane(Voter voter, EventHandler<ActionEvent> actionEventEventHandler) {

        TableColumn<FileVoteRecord, Image> imageColumn = createImageColumn();

        TableColumn<FileVoteRecord, String> votingColumn = new TableColumn<>(I18n.translate("summary.header.voting"));
        votingColumn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
        votingColumn.setCellValueFactory(new PropertyValueFactory<>("finalVoting"));

        TableColumn<FileVoteRecord, String> fileNameColumn = new TableColumn<>(I18n.translate("summary.header.filenameOld"));
        fileNameColumn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<FileVoteRecord, String> newFileNameColumn = new TableColumn<>(I18n.translate("summary.header.filenameNew"));
        newFileNameColumn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
        newFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("finalNewFilePath"));

        TableView<FileVoteRecord> tableView = new TableView<>();
        tableView.getColumns().addAll(imageColumn, votingColumn, fileNameColumn, newFileNameColumn);
        List<FileVoteRecord> fileRecordList = voter.generateFinalVoteResult();
        ObservableList<FileVoteRecord> data = FXCollections.observableArrayList(fileRecordList);

        tableView.setItems(data);

        setCenter(tableView);

        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(10));
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button cancelButton = new Button(I18n.translate("button.cancel"));
        Button renameButton = new Button(I18n.translate("button.rename"));
        cancelButton.setOnAction((e) -> quitAfterConfiguration());
        renameButton.setOnAction(actionEventEventHandler);
        buttonBox.getChildren().add(cancelButton);
        buttonBox.getChildren().add(renameButton);
        setBottom(buttonBox);
    }

    private TableColumn<FileVoteRecord, Image> createImageColumn() {
        TableColumn<FileVoteRecord, Image> imageColumn = new TableColumn<>(I18n.translate("summary.header.image"));
        imageColumn.setStyle("-fx-alignment: CENTER; -fx-padding: 5 10;"); // Hier können Sie die Werte entsprechend anpassen
        imageColumn.setCellValueFactory(param -> {
            String imagePath = param.getValue().getFilePath();
            if (imageCache.containsKey(imagePath)) {
                return imageCache.get(imagePath);
            }
            try {
                Image image = ImageService.createThumbnailInMemory(imagePath, 100, 100);
                ObjectProperty objectProperty = new SimpleObjectProperty<>(image);
                imageCache.put(imagePath, objectProperty);
                return objectProperty;
            } catch (Exception ex) {
                Logger.error(ex, "Failed to load image: {0}", imagePath);
                return null;
            }
        });
        imageColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    imageView.setFitWidth(75); // Setzen Sie die gewünschte Bildbreite
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });
        return imageColumn;
    }

    private void quitAfterConfiguration() {
        ConfirmationDialog dialog = new ConfirmationDialog("quit.confirm.question", "quit.confirm.yes", "quit.confirm.no");
        boolean confirmed = dialog.confirm().orElse(false);
        if (confirmed) {
            System.exit(0);
        }
    }

}
