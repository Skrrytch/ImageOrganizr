package eu.spex.iorg.component.pane;

import java.io.File;

import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.I18n;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class HeaderPane extends GridPane {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Mode mode;

    public HeaderPane(Mode mode, File directory, int size) {
        setHgap(10);
        setVgap(5);
        this.mode = mode;
        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setHgrow(Priority.ALWAYS); // Spalte passt sich automatisch an
        getColumnConstraints().add(columnConstraint);

        addModeTitle();
        addDirectoryInfo(directory + " (" + size + " files)");
        addModeDescription();
    }

    private void addModeTitle() {
        Label label = new Label(I18n.translate("mode." + mode.getParameter()));
        label.setStyle("-fx-font-size: 1.5em;");
        add(label, 0, 0);
    }

    private void addDirectoryInfo(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        add(label, 1, 0);
        GridPane.setHalignment(label, HPos.RIGHT);
    }

    private void addModeDescription() {
        Label label = new Label(I18n.translate("mode." + mode.getParameter() + ".description"));
        label.setWrapText(true);
        add(label, 0, 1, 2, 1);
    }

}
