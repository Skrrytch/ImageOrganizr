package eu.spex.iorg.component.pane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ImagePane extends VBox {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Mode mode;

    private final ImageView imageView;

    private final Label imageName;

    public ImagePane(Mode mode, boolean isLeftPane) {
        this.mode = mode;

        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);

        Pane imageViewPane = new Pane();
        imageViewPane.getChildren().add(imageView);
        this.imageName = new Label();
        getChildren().addAll(imageViewPane, imageName);
        VBox.setVgrow(imageViewPane, Priority.ALWAYS);
        VBox.setVgrow(imageName, Priority.NEVER);


        imageView.fitWidthProperty().bind(imageViewPane.widthProperty());
        imageView.fitHeightProperty().bind(imageViewPane.heightProperty());
    }

    public void setRecord(FileVoteRecord record, Consumer<FileVoteRecord> eventConsumer) throws FileNotFoundException {
        if (record == null) {
            clearRecord();
        } else {
            InputStream stream = new FileInputStream(record.getFilePath());
            Image image = new Image(stream);
            imageName.setText(record.getFileName());
            imageView.setImage(image);
            imageView.setOnMouseClicked((e) -> eventConsumer.accept(record));
        }
    }

    public void clearRecord() {
        if (imageView != null) {
            imageView.setImage(null);
        }
        if (imageName != null) {
            imageName.setText(null);
        }
    }
}
