package eu.spex.iorg.model;

import java.io.File;
import java.util.List;

public interface VoteResult {
    void setFiles(List<File> files);

    List<FileVoteRecord> getUnorderedRecords();

    List<FileVoteRecord> getOrderedRecords();
}
