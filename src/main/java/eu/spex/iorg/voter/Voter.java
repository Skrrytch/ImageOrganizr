package eu.spex.iorg.voter;

import java.io.File;
import java.util.List;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;
import eu.spex.iorg.model.VoteCheck;
import eu.spex.iorg.model.VoteResult;

public abstract class Voter {

    protected Mode mode;

    public Voter(Mode mode) {
        this.mode = mode;
    }

    public abstract boolean initCollection(List<File> files);

    abstract public Vote getStartVote();

    abstract public Vote vote(FileVoteRecord record, String voteValue);

    public VoteCheck checkVote(FileVoteRecord record, String value) {
        return null;
    }

    public abstract int getSize();

    public Mode getMode() {
        return mode;
    }

    public abstract VoteResult getVoteResult();


    public abstract List<FileVoteRecord> generateFinalVoteResult();

    public String getDefaultVote() {
        return "1";
    }

    public boolean supportsUndo() {
        return false;
    }

    public Vote undo() {
        return null;
    }

    public boolean supportsRestart() {
        return false;
    }

    public Vote restart() {
        return null;
    }
}
