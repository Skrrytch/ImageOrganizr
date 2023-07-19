package eu.spex.iorg.component.pane;

import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.I18n;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class FooterPane extends BorderPane {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Mode mode;
    private final Label stageLabel;

    private final HBox rightTools;
    private final HBox leftTools;

    public FooterPane(Mode mode) {
        this.mode = mode;
        this.stageLabel = createStageInfo();
        this.rightTools = new HBox();
        this.leftTools = new HBox();
        setLeft(leftTools);
        setCenter(stageLabel);
        setRight(rightTools);
    }

    private Label createStageInfo() {
        Label label = new Label("");
        label.setStyle("-fx-font-weight: bold;");
        label.setWrapText(true);
        return label;
    }

    public void setStage(String stageInfo) {
        stageLabel.setText(stageInfo);
    }

    public void enableUndo(EventHandler<ActionEvent> actionHandler) {
        Button undoButton = new Button(I18n.translate("button.undo"));
        undoButton.setOnAction(actionHandler);
        rightTools.getChildren().add(undoButton);
    }

    public void enableRestart(EventHandler<ActionEvent> actionHandler) {
        Button restButton = new Button(I18n.translate("button.restart"));
        restButton.setOnAction(actionHandler);
        leftTools.getChildren().add(restButton);
    }
}
