package eu.spex.iorg.component.pane;

import eu.spex.iorg.model.Mode;
import eu.spex.iorg.service.I18n;
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
        addMode(mode.getParameter());
        // Amount of files
        Label label = addValue(directory + " (" + size + " files)");
        GridPane.setHalignment(label, HPos.RIGHT);

        // Beschreibung
        label = new Label(I18n.translate("mode." + mode.getParameter() + ".description"));
        label.setWrapText(true);
        add(label, 0, 1, 2, 1);
    }

    private void addMode(String key) {
        Label label = new Label(I18n.translate("mode." + key));
        add(label, 0, 0);
        label.setStyle("-fx-font-size: 1.5em;");
    }

    private Label addValue(String text) {
        Label textControl = new Label(text);
        add(textControl, 1, 0);
        textControl.setWrapText(true);
        return textControl;
    }
}
