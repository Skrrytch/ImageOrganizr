package eu.spex.iorg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.spex.iorg.component.dialog.ConfirmationDialog;
import eu.spex.iorg.component.dialog.SelectModeDialog;
import eu.spex.iorg.component.pane.CategoryColors;
import eu.spex.iorg.component.pane.FinalSummaryPane;
import eu.spex.iorg.component.pane.FooterPane;
import eu.spex.iorg.component.pane.HeaderPane;
import eu.spex.iorg.component.pane.ImagePane;
import eu.spex.iorg.component.pane.VoteByCategoryPane;
import eu.spex.iorg.component.pane.VoteByRatingPane;
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
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX App
 */
public class ImageOrganizr extends Application {
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    private static final Duration SWAP_DURATION = Duration.millis(260);

    /** The left image starts moving later, so the right one clearly leads the swap. */
    private static final Duration SWAP_STAGGER = Duration.millis(90);

    /** How long the decided pair stays visible in its final order before it is faded out. */
    private static final Duration SETTLE_HOLD = Duration.millis(90);

    private static final Duration SETTLE_DURATION = Duration.millis(130);

    private static final Duration APPEAR_DURATION = Duration.millis(150);

    private boolean compareAnimating;

    /** Shared so a category keeps the same colour in the chip bar and in the summary. */
    private final CategoryColors categoryColors = new CategoryColors();

    private Mode mode;

    private StackPane rootPane;

    private ImagePane leftImagePane;

    private ImagePane rightImagePane;

    private VoteByCategoryPane rightCategorizePane;

    private VoteByRatingPane rightRatingPane;

    private HeaderPane headerPane;

    private FooterPane footerPane;

    private Voter voter;

    private Vote currentVote;

    public ImageOrganizr() {

    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        initLocale();

        File directory = initDirectory();
        if (directory == null) {
            Logger.error("Failed to find directory");
            return;
        }
        File[] files = getFilesFromDirectory(directory);
        if (files == null) {
            Logger.error("Failed to find files in " + directory.getAbsolutePath());
            return;
        }

        mode = getSortMode(files);
        if (mode == null) {
            return;
        }

        voter = switch (mode) {
            case SIMPLE_KNOCKOUT, FULL_KNOCKOUT -> new TournamentVoter(mode);
            case ORDER -> new OrderByMergeSortVoter(mode);
            case RATE, CATEGORIZE -> new CategorizeVoter(mode);
        };
        boolean success = voter.initCollection(Arrays.stream(files).collect(Collectors.toList()));
        if (!success) {
            System.exit(1);
        }
        currentVote = voter.getStartVote();

        primaryStage.setTitle("iorg: " + I18n.translate("mode." + mode.getParameter()));
        // Information Pane

        this.headerPane = createHeaderPane(mode, directory);
        this.footerPane = createFooterPane(mode, voter);
        Pane leftPane = createLeftPane(mode);
        Pane rightPane = createRightPane(mode);

        Pane contentPane = createContentPane(mode, leftPane, rightPane);

        VBox applicationPane = new VBox(10);
        applicationPane.getChildren().addAll(headerPane, new Separator(), contentPane, new Separator(), footerPane);
        HBox.setHgrow(footerPane, Priority.ALWAYS);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        rootPane = new StackPane();
        rootPane.getChildren().add(applicationPane);
        rootPane.setPadding(new Insets(10, 10, 10, 10));

        primaryStage.setOnCloseRequest(e -> System.exit(0));

        Scene scene = createScene(rootPane);
        initKeyboardShortcuts(scene);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(Math.min(scene.getWidth(), 750));
        primaryStage.setMinHeight(Math.min(scene.getHeight(), 750));

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Verhindert das Standard-Schließen des Fensters
            ConfirmationDialog dialog = new ConfirmationDialog("quit.confirm.question", "quit.confirm.yes", "quit.confirm.no");
            if (dialog.confirm().orElse(false)) {
                primaryStage.close();
            }
        });

        showCurrentVote();

        primaryStage.show();

        // keeps the initial focus away from the input field, so the voting keys work at once
        rootPane.requestFocus();
    }

    private Pane createContentPane(Mode mode, Pane leftPane, Pane rightPane) {
        if (mode.isCompareMode()) {
            // the images are the controls here, so they only get a hint line below them
            leftImagePane.setKeyHint("⏎");
            rightImagePane.setKeyHint("←");

            HBox imageRow = new HBox(10, leftPane, rightPane);
            HBox.setHgrow(leftPane, Priority.ALWAYS);
            HBox.setHgrow(rightPane, Priority.ALWAYS);

            VBox contentPane = new VBox(6, imageRow, createHintLabel("mode.compare.hint"));
            VBox.setVgrow(imageRow, Priority.ALWAYS);
            return contentPane;
        }
        // rate and categorize: controls docked below, so the image gets the full window width
        VBox contentPane = new VBox(10);
        contentPane.getChildren().addAll(leftPane, rightPane);
        VBox.setVgrow(leftPane, Priority.ALWAYS);
        VBox.setVgrow(rightPane, Priority.NEVER);
        return contentPane;
    }

    private Label createHintLabel(String key) {
        Label hint = new Label(I18n.translate(key));
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER);
        hint.setStyle("-fx-text-fill: #808080; -fx-font-size: 11px;");
        return hint;
    }

    private void initKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (mode) {
                case RATE -> handleRatingKey(event);
                case CATEGORIZE -> handleCategoryKey(event);
                case ORDER, SIMPLE_KNOCKOUT, FULL_KNOCKOUT -> handleCompareKey(event);
            }
        });
    }

    private void handleRatingKey(KeyEvent event) {
        int rating = ratingForKey(event.getCode());
        if (rating > 0 && rating <= rightRatingPane.getMaxRating()) {
            rightRatingPane.selectRating(rating);
            event.consume();
        }
    }

    private void handleCategoryKey(KeyEvent event) {
        if (rightCategorizePane.isTypingNewCategory()) {
            return;
        }
        int position = ratingForKey(event.getCode());
        if (position > 0 && position <= VoteByCategoryPane.MAX_HOTKEYS) {
            rightCategorizePane.selectCategoryByPosition(position);
            event.consume();
        }
    }

    private void handleCompareKey(KeyEvent event) {
        if (currentVote == null) {
            return;
        }
        switch (event.getCode()) {
            case ENTER -> {
                keepOrderAndVote();
                event.consume();
            }
            case LEFT -> {
                swapAndVote();
                event.consume();
            }
            default -> {
            }
        }
    }

    /**
     * The left image already comes first: both images pulse to confirm the order stays as it is.
     */
    private void keepOrderAndVote() {
        FileVoteRecord record = currentVote.getRecord1();
        if (compareAnimating || record == null) {
            return;
        }
        compareAnimating = true;
        leftImagePane.setHighlighted(true);
        rightImagePane.setHighlighted(true);

        ParallelTransition pulse = new ParallelTransition(leftImagePane.createPulse(), rightImagePane.createPulse());
        pulse.setOnFinished(e -> {
            leftImagePane.setHighlighted(false);
            rightImagePane.setHighlighted(false);
            settleAndVote(record);
        });
        pulse.play();
    }

    /**
     * The right image belongs in front: it moves over to the left first and the left image follows
     * to the other side slightly later, so it reads as "this one moves to the front".
     */
    private void swapAndVote() {
        FileVoteRecord record = currentVote.getRecord2();
        if (compareAnimating || record == null) {
            return;
        }
        compareAnimating = true;
        double distance = rightImagePane.getBoundsInParent().getMinX() - leftImagePane.getBoundsInParent().getMinX();

        TranslateTransition moveLeft = new TranslateTransition(SWAP_DURATION, rightImagePane);
        moveLeft.setFromX(0);
        moveLeft.setToX(-distance);

        TranslateTransition moveRight = new TranslateTransition(SWAP_DURATION, leftImagePane);
        moveRight.setFromX(0);
        moveRight.setToX(distance);
        moveRight.setDelay(SWAP_STAGGER);

        ParallelTransition swap = new ParallelTransition(moveLeft, moveRight);
        // the panes keep their swapped position until settleAndVote fades them out, so the chosen
        // order is actually visible for a moment instead of snapping back
        swap.setOnFinished(e -> settleAndVote(record));
        swap.play();
    }

    /**
     * Lets the pair rest briefly in its final order, fades it out and only then brings in the next
     * pair - the pause makes clear that this comparison is settled.
     */
    private void settleAndVote(FileVoteRecord record) {
        ParallelTransition settle = new ParallelTransition(
                createSettle(leftImagePane), createSettle(rightImagePane));
        settle.setDelay(SETTLE_HOLD);
        settle.setOnFinished(e -> {
            leftImagePane.setTranslateX(0);
            rightImagePane.setTranslateX(0);
            compareAnimating = false;
            handleVote(record);
            new ParallelTransition(createAppear(leftImagePane), createAppear(rightImagePane)).play();
        });
        settle.play();
    }

    private Animation createSettle(Node pane) {
        FadeTransition fade = new FadeTransition(SETTLE_DURATION, pane);
        fade.setFromValue(1);
        fade.setToValue(0);
        ScaleTransition shrink = new ScaleTransition(SETTLE_DURATION, pane);
        shrink.setFromX(1);
        shrink.setFromY(1);
        shrink.setToX(0.96);
        shrink.setToY(0.96);
        return new ParallelTransition(fade, shrink);
    }

    private Animation createAppear(Node pane) {
        FadeTransition fade = new FadeTransition(APPEAR_DURATION, pane);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition grow = new ScaleTransition(APPEAR_DURATION, pane);
        grow.setFromX(0.96);
        grow.setFromY(0.96);
        grow.setToX(1);
        grow.setToY(1);
        return new ParallelTransition(fade, grow);
    }

    private static int ratingForKey(KeyCode keyCode) {
        return switch (keyCode) {
            case DIGIT1, NUMPAD1 -> 1;
            case DIGIT2, NUMPAD2 -> 2;
            case DIGIT3, NUMPAD3 -> 3;
            case DIGIT4, NUMPAD4 -> 4;
            case DIGIT5, NUMPAD5 -> 5;
            case DIGIT6, NUMPAD6 -> 6;
            case DIGIT7, NUMPAD7 -> 7;
            case DIGIT8, NUMPAD8 -> 8;
            case DIGIT9, NUMPAD9 -> 9;
            case DIGIT0, NUMPAD0 -> 10;
            default -> 0;
        };
    }

    private Scene createScene(StackPane rootPane) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();
        double windowHeight = screenHeight - 50;
        double windowWidth = Math.min(screenWidth - 50, windowHeight * 1.5);
        return new Scene(rootPane, windowWidth, windowHeight);
    }

    private static File[] getFilesFromDirectory(File directory) {
        if (directory == null) {
            return null;
        }
        return directory.listFiles((dir, name) -> SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith));
    }

    private void initLocale() {
        Map<String, String> named = getParameters().getNamed();
        if (named.containsKey("lang")) {
            String languageLocale = named.get("lang");
            I18n.setLocale(languageLocale);
        }
    }

    private File initDirectory() {
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
            Logger.warn("No valid directory: {0}", directoryPath);
            return null;
        }
        return directory;
    }

    private HeaderPane createHeaderPane(Mode mode, File directory) {
        return new HeaderPane(mode, directory, voter.getSize());
    }

    private FooterPane createFooterPane(Mode mode, Voter voter) {
        FooterPane toolPane = new FooterPane(mode);
        if (voter.supportsRestart()) {
            toolPane.enableRestart((e) -> handleRestart());
        }
        if (voter.supportsUndo()) {
            toolPane.enableUndo((e) -> handleUndo());
        }
        return toolPane;
    }

    private Pane createLeftPane(Mode mode) {
        leftImagePane = new ImagePane(mode);
        return leftImagePane;
    }

    private Pane createRightPane(Mode mode) {
        if (mode.isCompareMode()) {
            rightImagePane = new ImagePane(mode);
            return rightImagePane;
        } else if (mode == Mode.CATEGORIZE) {
            rightCategorizePane = new VoteByCategoryPane(List.of(), categoryColors);
            return rightCategorizePane;
        } else if (mode == Mode.RATE) {
            rightRatingPane = new VoteByRatingPane(10);
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

    private void handleVote(FileVoteRecord record, String tag) {
        currentVote = voter.vote(record, tag);
        try {
            showCurrentVote();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void handleVote(FileVoteRecord record) {
        currentVote = voter.vote(record, voter.getDefaultVote());
        try {
            showCurrentVote();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUndo() {
        if (!voter.supportsUndo()) {
            Logger.error("Voter " + voter.getClass().getSimpleName() + " does not support UNDO!");
            return;
        }
        currentVote = voter.undo();
        try {
            showCurrentVote();
            if (rightCategorizePane != null) {
                rightCategorizePane.resetPreview();
            }
            if (rightRatingPane != null) {
                rightRatingPane.resetPreview();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRestart() {
        if (!voter.supportsRestart()) {
            Logger.error("Voter " + voter.getClass().getSimpleName() + " does not support RESET!");
            return;
        }
        currentVote = voter.restart();
        try {
            showCurrentVote();
            if (rightCategorizePane != null) {
                rightCategorizePane.resetPreview();
            }
            if (rightRatingPane != null) {
                rightRatingPane.resetPreview();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void showCurrentVote() throws FileNotFoundException {
        if (currentVote == null) {
            clearOnFinish();
            FinalSummaryPane summaryPane = new FinalSummaryPane(voter, categoryColors, (e) -> {
                renameAll();
                System.exit(0);
            });
            this.rootPane.getChildren().add(summaryPane);
        } else {
            showVote(currentVote);
        }
    }

    private void showVote(Vote currentVote) throws FileNotFoundException {
        FileVoteRecord record = currentVote.getRecord1();
        if (mode.isCompareMode()) {
            // a click means the same as the corresponding key, so it gets the same animation
            leftImagePane.setRecord(record, r -> keepOrderAndVote());
            rightImagePane.setRecord(currentVote.getRecord2(), r -> swapAndVote());
        } else {
            leftImagePane.setRecord(record, this::handleVote);
        }
        if (mode == Mode.CATEGORIZE) {
            rightCategorizePane.setRecord(
                    record,
                    (tag) -> this.handleVote(record, tag),
                    (tag) -> this.previewTagSelection(record, tag));
        } else if (mode == Mode.RATE) {
            rightRatingPane.setRecord(
                    record,
                    (tag) -> this.handleVote(record, tag),
                    (tag) -> this.previewTagSelection(record, tag));
        }
        footerPane.setStage(currentVote.getStageDescription());
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
        footerPane.setStage("Beendet");
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