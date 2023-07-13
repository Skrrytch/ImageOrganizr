package eu.spex.imagesort.component.pane;

import eu.spex.imagesort.model.Mode;
import eu.spex.imagesort.service.I18n;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class InformationPane extends GridPane {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Mode mode;

    public InformationPane(Mode mode, String directory, int size) {
        setHgap(10);
        setVgap(5);
        this.mode = mode;
        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setHgrow(Priority.ALWAYS); // Spalte passt sich automatisch an
        getColumnConstraints().add(columnConstraint);

        // Modus
        addMode(mode.getParameter(), 0, 0);
        // Amount of files
        Label label = addValue(directory + " (" + size + " files)", 1, 0);
        GridPane.setHalignment(label, HPos.RIGHT);

        // Beschreibung
        label = new Label(I18n.translate("mode." + mode.getParameter() + ".description"));
        label.setWrapText(true);
        add(label, 0, 1, 2, 1);
    }

    private void addMode(String key, int column, int row) {
        Label label = new Label(I18n.translate("mode." + key));
        add(label, column, row);
        label.setStyle("-fx-font-size: 1.5em;");
    }

    private Label addValue(String text, int column, int row) {
        Label textControl = new Label(text);
        add(textControl, column, row);
        textControl.setWrapText(true);
        return textControl;
    }
}
