package eu.spex.imagesort.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import eu.spex.imagesort.model.FileVoteRecord;
import eu.spex.imagesort.model.Mode;
import eu.spex.imagesort.model.VoteResult;

public class CategorizeVoteResult implements VoteResult {

    private final Mode mode;

    private List<FileVoteRecord> allRecords;

    private Map<String, List<FileVoteRecord>> categorizedRecords;

    private int votingElementIdx = 0;

    public CategorizeVoteResult(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void setFiles(List<File> files) {
        if (files.size() == 0) {
            throw new IllegalStateException("No images found");
        }
        this.allRecords = files.stream().map(file -> new FileVoteRecord(file.getAbsolutePath(), mode)).toList();
        this.categorizedRecords = new TreeMap<>();
        this.votingElementIdx = 0;
    }

    @Override
    public List<FileVoteRecord> getUnorderedRecords() {
        return new ArrayList<>(this.allRecords);
    }

    @Override
    public List<FileVoteRecord> getOrderedRecords() {
        return new ArrayList<>(this.allRecords);
    }

    public FileVoteRecord getNext() {
        if (votingElementIdx < allRecords.size()) {
            return allRecords.get(votingElementIdx++);
        } else {
            return null;
        }
    }

    public String getStageDescription() {
        return "Categorizing " + votingElementIdx + " of " + allRecords.size();
    }

    public void onVoted(FileVoteRecord record) {
        List<FileVoteRecord> recordList = categorizedRecords.computeIfAbsent(
                record.getFinalVoting(),
                k -> new ArrayList<>());
        recordList.add(record);
    }

    public List<FileVoteRecord> getCategoryRecords(String category) {
        return categorizedRecords.get(category);
    }
}
