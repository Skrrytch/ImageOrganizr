package eu.spex.iorg.model;

import java.nio.file.Path;
import java.util.Objects;


public class FileVoteRecord {

    private final Mode mode;

    private final String filePath;

    private final String fileName;

    private FileRename finalFileRename;

    private String finalVoting;

    public FileVoteRecord(String filePath, Mode mode) {
        this.mode = mode;
        this.filePath = filePath;
        this.fileName = Path.of(filePath).getFileName().toString();
    }

    public Mode getMode() {
        return mode;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }


    public void setFinalResult(String votingValue, FileRename rename) {
        this.finalVoting = votingValue;
        this.finalFileRename = rename;
    }

    public FileRename getFinalFileRename() {
        return finalFileRename;
    }

    @SuppressWarnings("unused") // Used per reflection from JavaFX
    public String getFinalNewFilePath() {
        if (finalFileRename.getNewDirectory() != null) {
            return finalFileRename.getNewDirectory() + "/" + finalFileRename.getNewFilename();
        } else {
            return finalFileRename.getNewFilename();
        }
    }

    public String getFinalVoting() {
        return finalVoting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileVoteRecord that = (FileVoteRecord) o;
        return Objects.equals(filePath, that.filePath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    @Override
    public String toString() {
        return "FileVoteRecord{" +
                "fileName='" + fileName + '\'' +
                '}';
    }

}
