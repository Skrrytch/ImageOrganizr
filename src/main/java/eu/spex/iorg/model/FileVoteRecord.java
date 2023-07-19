package eu.spex.iorg.model;

import java.nio.file.Path;
import java.util.Objects;

import eu.spex.iorg.service.Logger;

public class FileVoteRecord {

    private final Mode mode;

    private final String filePath;

    private final String fileName;

    private FileRename finalFileRename;

    private int countOfVotes;

    private int countOfVotings;

    private String finalVoting;

    public FileVoteRecord(String filePath, Mode mode) {
        this.mode = mode;
        this.filePath = filePath;
        this.fileName = Path.of(filePath).getFileName().toString();
    }

    public Mode getMode() {
        return mode;
    }

    public void vote() {
        this.countOfVotes++;
    }

    public void countVoting() {
        this.countOfVotings++;
        Logger.info(fileName + " voting count = " + countOfVotings);
    }

    public void resetVotingCount() {
        this.countOfVotes = 0;
        this.countOfVotings = 0;
        Logger.info(fileName + " voting count reset to " + countOfVotings);
    }

    public void undoVotingCount() {
        this.countOfVotings--;
        Logger.info(fileName + " voting count reverted to " + countOfVotings);
    }

    public int getCountOfVotings() {
        return countOfVotings;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }


    public int getCountOfVotes() {
        return countOfVotes;
    }

    public void setFinalResult(String votingValue, FileRename rename) {
        this.finalVoting = votingValue;
        this.finalFileRename = rename;
    }

    public FileRename getFinalFileRename() {
        return finalFileRename;
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
