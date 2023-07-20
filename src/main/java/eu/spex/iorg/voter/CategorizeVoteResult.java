package eu.spex.iorg.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.VoteResult;
import eu.spex.iorg.service.Logger;

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

    public boolean undo() {
        if (votingElementIdx <= 1) {
            return false;
        }
        votingElementIdx--; // goto current element (idx is already set to the next element!)
        votingElementIdx--; // goto undo element
        FileVoteRecord fileVoteRecord = allRecords.get(votingElementIdx);
        boolean removed = categorizedRecords.get(fileVoteRecord.getFinalVoting()).remove(fileVoteRecord);
        if (removed) {
            Logger.info("Voting undone for ''{0}''", fileVoteRecord.getFileName());
        } else {
            Logger.warn("Failed to remove undone voting for ''{0}''", fileVoteRecord.getFileName());
        }
        return true;
    }

    public boolean restart() {
        votingElementIdx = 0;
        categorizedRecords.clear();
        return true;
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
