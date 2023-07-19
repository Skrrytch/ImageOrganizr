package eu.spex.iorg.voter;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import eu.spex.iorg.model.FileRename;
import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.model.VoteResult;
import eu.spex.iorg.service.Logger;

public class CategorizeVoter extends Voter {

    private final CategorizeVoteResult voteResult;
    private int size;

    public CategorizeVoter(Mode mode) {
        super(mode);
        voteResult = new CategorizeVoteResult(mode);
    }

    @Override
    public boolean initCollection(List<File> files) {
        if (files.isEmpty()) {
            Logger.warn("No images in directory found.");
            return false;
        }

        List<File> sortedFiles = files.stream().sorted(Comparator.comparing(File::getAbsolutePath)).collect(Collectors.toList());
        voteResult.setFiles(sortedFiles);

        size = files.size();
        return true;
    }

    @Override
    public Vote vote(FileVoteRecord record, String voteValue) {
        record.setFinalResult(voteValue, generateNewFilepath(record, voteValue));
        voteResult.onVoted(record);
        return getNextVote();
    }

    @Override
    public boolean supportsUndo() {
        return true;
    }

    public Vote undo() {
        if (voteResult.undo()) {
            return getNextVote();
        } else {
            Logger.warn("Undo not possible");
            return getNextVote();
        }
    }

    @Override
    public boolean supportsRestart() {
        return true;
    }

    @Override
    public Vote restart() {
        if (voteResult.restart()) {
            return getNextVote();
        } else {
            Logger.warn("Undo not possible");
            return getNextVote();
        }
    }

    @Override
    public VoteCheck checkVote(FileVoteRecord record, String voteValue) {
        List<FileVoteRecord> records = voteResult.getCategoryRecords(voteValue);
        return new VoteCheck(records);
    }

    @Override
    public Vote getStartVote() {
        return getNextVote();
    }

    private Vote getNextVote() {
        FileVoteRecord nextRecord = voteResult.getNext();
        if (nextRecord == null) {
            return null;
        }
        Vote vote = new Vote(voteResult.getStageDescription(), nextRecord, null);
        vote.countVoting();
        return vote;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public VoteResult getVoteResult() {
        return voteResult;
    }

    @Override
    public List<FileVoteRecord> generateFinalVoteResult() {
        return voteResult.getOrderedRecords();
    }

    private FileRename generateNewFilepath(FileVoteRecord voteRecord, String voteValue) {
        if (mode == Mode.CATEGORIZE) {
            FileRename rename = new FileRename(voteRecord.getFilePath(), voteRecord.getFileName());
            rename.setNewDirectory(voteValue);
            rename.setNewFilename(voteRecord.getFileName());
            return rename;
        } else {
            FileRename rename = new FileRename(voteRecord.getFilePath(), voteRecord.getFileName());
            rename.setNewFilename(voteValue + "-" + voteRecord.getFileName());
            return rename;
        }
    }

    @Override
    public String getDefaultVote() {
        return switch (mode) {
            case SIMPLE_KNOCKOUT, ORDER, FULL_KNOCKOUT -> throw new IllegalStateException("Unexpected mode: " + mode);
            case RATE -> "10";
            case CATEGORIZE -> "default";
        };
    }

}
