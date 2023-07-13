package eu.spex.imagesort.voter;

import java.io.File;
import java.util.List;

import eu.spex.imagesort.model.FileVoteRecord;
import eu.spex.imagesort.model.Mode;
import eu.spex.imagesort.model.Vote;
import eu.spex.imagesort.model.VoteCheck;
import eu.spex.imagesort.model.VoteResult;

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
}
