package eu.spex.imagesort.model;

import java.util.List;

public class VoteCheck {

    private final List<FileVoteRecord> fileVoteRecords;

    public VoteCheck(List<FileVoteRecord> fileVoteRecords) {
        this.fileVoteRecords = fileVoteRecords;
    }

    public List<FileVoteRecord> getPreviewRecords() {
        return fileVoteRecords;
    }
}
