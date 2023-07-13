
package eu.spex.imagesort.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.spex.imagesort.model.FileVoteRecord;
import eu.spex.imagesort.model.Mode;
import eu.spex.imagesort.model.VoteResult;

public class OrderByMergeSortVoteResult implements VoteResult {

    private final Mode mode;

    private List<FileVoteRecord> unorderedRecords;
    private List<FileVoteRecord> orderedRecords;

    public OrderByMergeSortVoteResult(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void setFiles(List<File> files) {
        this.unorderedRecords = new ArrayList<>(files.stream()
                .map(file -> new FileVoteRecord(file.getAbsolutePath(), mode))
                .toList());
        this.orderedRecords = new ArrayList<>(this.unorderedRecords);
    }

    @Override
    public List<FileVoteRecord> getUnorderedRecords() {
        return unorderedRecords;
    }

    @Override
    public List<FileVoteRecord> getOrderedRecords() {
        return new ArrayList<>(this.orderedRecords);
    }

    public FileVoteRecord getRecord(int recordIndex) {
        if (orderedRecords.size() <= recordIndex) {
            return null;
        }
        return orderedRecords.get(recordIndex);
    }

    public void setRecord(int idx, FileVoteRecord fileVoteRecord) {
        orderedRecords.set(idx, fileVoteRecord);
    }

    public void onVoted(FileVoteRecord record) {
        // Nothing to do
    }
}
