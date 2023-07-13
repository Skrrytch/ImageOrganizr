package eu.spex.iorg.component.pane;

import eu.spex.iorg.model.Mode;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

public class StatusPane extends HBox {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Mode mode;
    private final Label stageControl;

    public StatusPane(Mode mode) {
        this.mode = mode;
        this.stageControl = addLabel("");
        this.stageControl.setAlignment(Pos.CENTER_RIGHT);
        this.stageControl.setTextAlignment(TextAlignment.RIGHT);
    }

    private Label addLabel(String text) {
        Label textControl = new Label(text);
        textControl.setStyle("-fx-font-weight: bold;");
        getChildren().add(textControl);
        textControl.setWrapText(true);
        GridPane.setValignment(textControl, VPos.TOP); // Top alignment
        return textControl;
    }

    public void setStage(String stageInfo) {
        stageControl.setText(stageInfo);
    }
}
