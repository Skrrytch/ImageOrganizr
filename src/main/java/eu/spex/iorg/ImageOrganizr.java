package eu.spex.iorg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.spex.iorg.component.dialog.ConfirmationDialog;
import eu.spex.iorg.component.pane.FinalSummaryPane;
import eu.spex.iorg.component.pane.ImagePane;
import eu.spex.iorg.component.pane.InformationPane;
import eu.spex.iorg.component.dialog.SelectModeDialog;
import eu.spex.iorg.component.pane.StatusPane;
import eu.spex.iorg.component.pane.VoteByCategoryPanel;
import eu.spex.iorg.component.pane.VoteByRatingPanel;
import eu.spex.iorg.model.FileRename;
import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.service.I18n;
import eu.spex.iorg.service.Logger;
import eu.spex.iorg.voter.CategorizeVoter;
import eu.spex.iorg.voter.OrderByMergeSortVoter;
import eu.spex.iorg.voter.TournamentVoter;
import eu.spex.iorg.voter.Voter;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class ImageOrganizr extends Application {
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    private Mode mode;

    private StackPane rootPane;

    private HBox contentPane;
    private VBox applicationPane;

    private ImagePane leftImagePane;

    private ImagePane rightImagePane;

    private VoteByCategoryPanel rightCategorizePane;

    private VoteByRatingPanel rightRatingPane;

    private InformationPane informationPane;

    private StatusPane statusPane;

    private Voter voter;

    private Vote currentVote;

    public ImageOrganizr() {

    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        File directory = getDirectory();
        if (directory == null) {
            return;
        }

        File[] files = directory.listFiles((dir, name) -> SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith));
        if (files == null) {
            Logger.error("Failed to find directory or files: " + directory.getAbsolutePath());
            return;
        }

        mode = getSortMode(files);
        if (mode == null) {
            return;
        }

        voter = switch (mode) {
            case SIMPLE_KNOCKOUT, FULL_KNOCKOUT -> new TournamentVoter(mode);
            case ORDER -> new OrderByMergeSortVoter(mode);
            case RATE -> new CategorizeVoter(mode);
            case CATEGORIZE -> new CategorizeVoter(mode);
        };
        boolean success = voter.initCollection(Arrays.stream(files).collect(Collectors.toList()));
        if (!success) {
            System.exit(1);
        }
        currentVote = voter.getStartVote();
        if (currentVote != null) {
            currentVote.countVoting();
        }

        primaryStage.setTitle("iorg: "+ I18n.translate("mode."+mode.getParameter()));
        // Information Pane

        createInformationPane(mode, directory.getAbsolutePath());
        createStatusPane(mode);
        Pane leftPane = createLeftPane(mode);
        Pane rightPane = createRightPane(mode);

        contentPane = new HBox(10);
        contentPane.getChildren().addAll(leftPane, rightPane);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        if (mode.isCompareMode()) {
            HBox.setHgrow(rightPane, Priority.ALWAYS);
        }

        applicationPane = new VBox(10);
        applicationPane.getChildren().addAll(informationPane, contentPane, statusPane);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        rootPane = new StackPane();
        rootPane.getChildren().add(applicationPane);
        rootPane.setPadding(new Insets(10, 10, 10, 10));

        primaryStage.setOnCloseRequest(e -> System.exit(0));

        primaryStage.setScene(new Scene(rootPane));
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(750);
        primaryStage.setWidth(750);
        primaryStage.setHeight(750);

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Verhindert das Standard-Schließen des Fensters
            ConfirmationDialog dialog = new ConfirmationDialog("quit.confirm.question", "quit.confirm.yes", "quit.confirm.no");
            if (dialog.confirm().orElse(false)) {
                primaryStage.close();
            }
        });

        nextImages();

        primaryStage.show();
    }

    private File getDirectory() {
        String directoryPath = Paths.get("").toAbsolutePath().toString();
        List<String> unnamedParams = getParameters().getUnnamed();
        if (unnamedParams.size() >= 1) {
            directoryPath = unnamedParams.get(0);
            Logger.info("Using directory from argument: " + directoryPath);
        } else {
            Logger.info("Using current directory: " + directoryPath);
        }

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.out.println("No valid directory: " + directoryPath);
            return null;
        }
        return directory;
    }

    private void createInformationPane(Mode mode, String directoryPath) {
        informationPane = new InformationPane(mode, directoryPath, voter.getSize());
    }

    private void createStatusPane(Mode mode) {
        statusPane = new StatusPane(mode);
    }

    private Pane createLeftPane(Mode mode) {
        leftImagePane = new ImagePane(mode, true);
        return leftImagePane;
    }

    private Pane createRightPane(Mode mode) {
        if (mode.isCompareMode()) {
            rightImagePane = new ImagePane(mode, false);
            return rightImagePane;
        } else if (mode == Mode.CATEGORIZE) {
            rightCategorizePane = new VoteByCategoryPanel(List.of());
            return rightCategorizePane;
        } else if (mode == Mode.RATE) {
            rightRatingPane = new VoteByRatingPanel(10);
            return rightRatingPane;
        }
        return null;
    }

    private Mode getSortMode(File[] files) {
        Parameters parameters = getParameters();
        String modeValue = parameters.getNamed().get("mode");
        if (modeValue != null) {
            mode = Mode.byParameter(modeValue);
            if (mode == null) {
                Logger.error("Mode ''{0}'' not supported.", modeValue);
            }
            return mode;
        } else {
            SelectModeDialog modeDialog = new SelectModeDialog(files.length);
            Optional<ButtonType> buttonType = modeDialog.showAndWait();
            return buttonType.isPresent() && buttonType.get() == SelectModeDialog.START
                    ? modeDialog.getMode()
                    : null;
        }
    }

    private void handleImageSelection(FileVoteRecord record) {
        currentVote = voter.vote(record, voter.getDefaultVote());
        if (currentVote != null) {
            currentVote.countVoting();
        }
        try {
            nextImages();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleTagSelection(FileVoteRecord record, String tag) {
        currentVote = voter.vote(record, tag);
        if (currentVote != null) {
            currentVote.countVoting();
        }
        try {
            nextImages();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void nextImages() throws FileNotFoundException {
        if (currentVote == null) {
            clearOnFinish();
            FinalSummaryPane summaryPane = new FinalSummaryPane(voter, (e) -> {
                renameAll();
                System.exit(0);
            });
            this.rootPane.getChildren().add(summaryPane);
        } else {
            initNext(currentVote);
        }
    }

    private void initNext(Vote currentVote) throws FileNotFoundException {
        FileVoteRecord record = currentVote.getRecord1();
        leftImagePane.setRecord(record, this::handleImageSelection);
        if (mode.isCompareMode()) {
            FileVoteRecord record2 = currentVote.getRecord2();
            rightImagePane.setRecord(record2, this::handleImageSelection);
        } else if (mode == Mode.CATEGORIZE) {
            rightCategorizePane.setRecord(
                    record,
                    (tag) -> this.handleTagSelection(record, tag),
                    (tag) -> this.previewTagSelection(record, tag));
        } else if (mode == Mode.RATE) {
            rightRatingPane.setRecord(
                    record,
                    (tag) -> this.handleTagSelection(record, tag),
                    (tag) -> this.previewTagSelection(record, tag));
        }
        statusPane.setStage(currentVote.getStageDescription());
    }

    private VoteCheck previewTagSelection(FileVoteRecord record, String tag) {
        return voter.checkVote(record, tag);
    }

    private void clearOnFinish() {
        leftImagePane.clearRecord();
        if (mode.isCompareMode()) {
            rightImagePane.clearRecord();
        } else if (mode == Mode.CATEGORIZE) {
            rightCategorizePane.clearRecord();
        }
        statusPane.setStage("Beendet");
        rootPane.getChildren().clear();
    }

    public void renameAll() {
        List<FileVoteRecord> allRecords = voter.getVoteResult().getUnorderedRecords();
        for (FileVoteRecord record : allRecords) {
            FileRename fileRename = record.getFinalFileRename();
            Path path = Paths.get(fileRename.getOriginalPath());
            Path parentDirectory = path.getParent();
            if (fileRename.getNewDirectory() != null) {
                parentDirectory = parentDirectory.resolve(fileRename.getNewDirectory());
                ensureExists(parentDirectory);
            }
            Path newPath = parentDirectory.resolve(fileRename.getNewFilename());
            try {
                Files.move(path, newPath);
                Logger.info("File renamed successfully to {0}", newPath.toString());
            } catch (IOException e) {
                Logger.error(e, "An error occurred while renaming the file: " + e.getMessage());
            }
        }
    }

    private void ensureExists(Path path) {
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path);
            Logger.info("Directory created: {0}", path);
        } catch (IOException e) {
            Logger.error(e, "Failed to create directory ''{0}''", path);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}