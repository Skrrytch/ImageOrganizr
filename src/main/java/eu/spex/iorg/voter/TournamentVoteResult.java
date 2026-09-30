package eu.spex.iorg.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.VoteResult;
import eu.spex.iorg.service.I18n;
import eu.spex.iorg.service.Logger;

public class TournamentVoteResult implements VoteResult {

    private final Mode mode;

    private List<FileVoteRecord> allRecords;

    private List<List<FileVoteRecord>> votingLists;

    private int votingListIdx = 0;

    private int votingListElementIdx = 0;

    public TournamentVoteResult(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void setFiles(List<File> files) {
        this.allRecords = files.stream().map(file -> new FileVoteRecord(file.getAbsolutePath(), mode)).toList();
        List<FileVoteRecord> initialRankedList = new ArrayList<>(allRecords);
        this.votingLists = new ArrayList<>();
        this.votingLists.add(initialRankedList);
        this.votingLists.add(new ArrayList<>()); // next stage
    }

    @Override
    public List<FileVoteRecord> getUnorderedRecords() {
        return new ArrayList<>(this.allRecords);
    }

    @Override
    public List<FileVoteRecord> getOrderedRecords() {
        return this.votingLists.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public FileVoteRecord getFirst() {
        List<FileVoteRecord> currentVotingList = votingLists.get(votingListIdx);
        if (votingListElementIdx < currentVotingList.size()) {
            FileVoteRecord fileVoteRecord = currentVotingList.get(votingListElementIdx);
            Logger.info("Next element (1st) in list (list idx {0}, element idx {1})", votingListIdx, votingListElementIdx);
            votingListElementIdx++;
            return fileVoteRecord;
        } else { // list done: continue with the next list (full knockout: the first list with more than one element)
            int newVotingListIdx = votingListIdx + 1;
            if (mode == Mode.FULL_KNOCKOUT) { // always begin with the first list that has more than one element!
                newVotingListIdx = 0;
                while (newVotingListIdx < votingLists.size() && votingLists.get(newVotingListIdx).size() <= 1) {
                    newVotingListIdx++;
                }
            }
            // end of voting?
            boolean finished = mode == Mode.FULL_KNOCKOUT
                    ? newVotingListIdx >= votingLists.size()
                    : votingLists.get(newVotingListIdx).size() == 1;
            if (finished) {
                Logger.info("Finished voting ({0} lists)", votingLists.size());
                return null;
            }
            votingLists.add(newVotingListIdx + 1, new ArrayList<>());
            Logger.info("Next voting round: New voting List added (idx {0})", votingListIdx + 1);
            votingListIdx = newVotingListIdx;
            votingListElementIdx = 0;
            return getFirst();
        }
    }

    public FileVoteRecord getSecond() {
        List<FileVoteRecord> currentVotingList = votingLists.get(votingListIdx);
        if (votingListElementIdx < currentVotingList.size()) {
            Logger.info("Next element (2nd) in list (list idx {0}, element idx {1})", votingListIdx, votingListElementIdx);
            FileVoteRecord fileVoteRecord = currentVotingList.get(votingListElementIdx);
            votingListElementIdx++;
            return fileVoteRecord;
        } else {
            Logger.info("No more elements in list. Using first one (again) for a vote.", votingListElementIdx);
            return currentVotingList.get(0); // TODO: should this vote count "out of competition"?
        }
    }

    public void onVoted(FileVoteRecord record) {
        List<FileVoteRecord> currentList = this.votingLists.get(votingListIdx);
        List<FileVoteRecord> nextList = this.votingLists.get(votingListIdx + 1);
        nextList.add(record);
        currentList.remove(record);
        this.votingListElementIdx--;

    }

    public String getStageDescription() {
        int finishedVotingCount = votingListIdx;
        return mode == Mode.FULL_KNOCKOUT
                ? I18n.translate("stage.fullKnockout", finishedVotingCount, allRecords.size() - finishedVotingCount)
                : I18n.translate("stage.simpleKnockout", finishedVotingCount + 1);
    }

    public List<List<FileVoteRecord>> getListsOfRecords() {
        return this.votingLists;
    }
}
