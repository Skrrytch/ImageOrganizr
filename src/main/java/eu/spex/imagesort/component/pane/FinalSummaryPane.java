package eu.spex.imagesort.component.pane;

import java.util.List;

import eu.spex.imagesort.component.dialog.ConfirmationDialog;
import eu.spex.imagesort.model.FileVoteRecord;
import eu.spex.imagesort.service.I18n;
import eu.spex.imagesort.voter.Voter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class FinalSummaryPane extends BorderPane {


    private TableView<FileVoteRecord> tableView;
    private ObservableList<FileVoteRecord> data;

    public FinalSummaryPane(Voter voter, EventHandler<ActionEvent> actionEventEventHandler) {
        TableColumn<FileVoteRecord, String> fileNameColumn = new TableColumn<>("Dateiname");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<FileVoteRecord, Integer> countOfVotingsColumn = new TableColumn<>("Votings");
        countOfVotingsColumn.setCellValueFactory(new PropertyValueFactory<>("countOfVotings"));

        TableColumn<FileVoteRecord, String> votingColumn = new TableColumn<>("Value");
        votingColumn.setCellValueFactory(new PropertyValueFactory<>("finalVoting"));

        TableColumn<FileVoteRecord, String> newFileNameColumn = new TableColumn<>("Neuer Dateiname");
        newFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("finalNewFilePath"));

        tableView = new TableView<>();
        tableView.getColumns().addAll(fileNameColumn, countOfVotingsColumn, votingColumn, newFileNameColumn);
        List<FileVoteRecord> fileRecordList = voter.generateFinalVoteResult();
        data = FXCollections.observableArrayList(fileRecordList);

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

    private void quitAfterConfiguration() {
        ConfirmationDialog dialog = new ConfirmationDialog("quit.confirm.question","quit.confirm.yes","quit.confirm.no");
        boolean confirmed = dialog.confirm().orElse(false);
        if (confirmed) {
            System.exit(0);
        }
    }

}
