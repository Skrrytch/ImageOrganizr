package eu.spex.imagesort.component.dialog;

import java.util.Optional;

import eu.spex.imagesort.service.I18n;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class ConfirmationDialog extends Dialog<Boolean> {

    public ConfirmationDialog(String textKey, String yesButtonKey, String noButtonKey) {

        setTitle("Bestätigung");
        setHeaderText(I18n.translate(textKey));

        ButtonType cancelButtonType = new ButtonType(I18n.translate(noButtonKey));
        ButtonType confirmButtonType = new ButtonType(I18n.translate(yesButtonKey));

        getDialogPane().getButtonTypes().addAll(cancelButtonType, confirmButtonType);

        setResultConverter(dialogButton -> {
            // Hier können Sie die Aktion ausführen, die beim Bestätigen des Dialogs ausgeführt werden soll
            return dialogButton == confirmButtonType;
        });
    }

    public Optional<Boolean> confirm() {
        return showAndWait();
    }
}
