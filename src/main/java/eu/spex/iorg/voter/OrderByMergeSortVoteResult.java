
package eu.spex.iorg.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Stack;
import eu.spex.iorg.model.VoteResult;
import eu.spex.iorg.service.Logger;

public class OrderByMergeSortVoteResult implements VoteResult {

    private final Mode mode;

    private List<FileVoteRecord> unorderedRecords;


    private OrderByMergeSortVotingState state;

    private final Stack<OrderByMergeSortVotingState> previousStates = new Stack<>();

    public OrderByMergeSortVoteResult(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void setFiles(List<File> files) {
        this.unorderedRecords = new ArrayList<>(files.stream()
                .map(file -> new FileVoteRecord(file.getAbsolutePath(), mode))
                .toList());


        this.state = new OrderByMergeSortVotingState(unorderedRecords);
    }

    @Override
    public List<FileVoteRecord> getUnorderedRecords() {
        return unorderedRecords;
    }

    @Override
    public List<FileVoteRecord> getOrderedRecords() {
        return new ArrayList<>(state.orderedRecords);
    }

    public FileVoteRecord getRecord(int recordIndex) {
        if (state.orderedRecords.size() <= recordIndex) {
            return null;
        }
        return state.orderedRecords.get(recordIndex);
    }

    public void setRecord(int idx, FileVoteRecord fileVoteRecord) {
        state.orderedRecords.set(idx, fileVoteRecord);
    }

    public List<FileVoteRecord> vote(FileVoteRecord record, String voteValue) {
        previousStates.push(state.copy());

        state.compareCount++;

        Logger.info("Loop 3: Voted. Updating indexes");
        // Finished inner loop after vote: while (i < leftSize && j < rightSize) ...
        if (record == state.leftListRecord) {
            setRecord(state.idxList, state.loop2_leftList.get(state.idxLeft));
            state.idxLeft++;
        } else if (record == state.rightListRecord) {
            setRecord(state.idxList, state.loop2_rightList.get(state.idxRight));
            state.idxRight++;
        } else {
            throw new IllegalArgumentException("Unexpected record.");
        }
        state.idxList++;
        // Finished all loops

        // After loop: while (i < leftSize && j < rightSize) ...
        // which is the end of loop: while (leftStart < n - blockSize) ...
        if (state.idxLeft >= state.loop2_leftSize || state.idxRight >= state.loop2_rightSize) {
            Logger.info("Loop 3: Ended. Finishing iteration of loop 2");
            while (state.idxLeft < state.loop2_leftSize) {
                setRecord(state.idxList, state.loop2_leftList.get(state.idxLeft));
                state.idxLeft++;
                state.idxList++;
            }

            while (state.idxRight < state.loop2_rightSize) {
                setRecord(state.idxList, state.loop2_rightList.get(state.idxRight));
                state.idxRight++;
                state.idxList++;
            }

            state.loop1_leftStart += 2 * state.glob_blockSize;
            state.loop2nextIteration = true;
        }

        // After loop: while (leftStart < n - blockSize) ...
        // which is the end of loop: while (blockSize < n) ...
        if (state.loop1_leftStart >= state.size - state.glob_blockSize) {
            Logger.info("Loop 2: Ended. Finishing iteration of loop 1");
            state.glob_blockSize *= 2;
            state.loop1nextIteration = true;
        }
        return getNextVote();
    }


    public List<FileVoteRecord> getNextVote() {
        // First loop (end criterium): while (blockSize < n)...
        if (state.glob_blockSize >= state.size) {
            Logger.info("Main loop ends");
            return null;
        }
        // New iteration of loop 1: while (blockSize < n)...
        if (state.loop1nextIteration) {
            state.loop1nextIteration = false;
            Logger.info("Loop 1: New iteration");
            state.loop1_leftStart = 0;
        }

        // Next iteration of loop 2: while (leftStart < n - blockSize) ...
        if (state.loop2nextIteration) {
            state.loop2nextIteration = false;
            Logger.info("Loop 2: New iteration");
            int loop2_mid = state.loop1_leftStart + state.glob_blockSize - 1;
            int loop2_rightEnd = Math.min(state.loop1_leftStart + 2 * state.glob_blockSize - 1, state.size - 1);

            state.loop2_leftSize = loop2_mid - state.loop1_leftStart + 1;
            state.loop2_leftList = new ArrayList<>();
            for (int i = 0; i < state.loop2_leftSize; i++) {
                state.loop2_leftList.add(getRecord(state.loop1_leftStart + i));
            }

            state.loop2_rightSize = loop2_rightEnd - loop2_mid;
            state.loop2_rightList = new ArrayList<>();
            for (int i = 0; i < state.loop2_rightSize; i++) {
                state.loop2_rightList.add(getRecord(loop2_mid + i + 1));
            }

            state.idxLeft = 0;
            state.idxRight = 0;
            state.idxList = state.loop1_leftStart;
        }

        Logger.info("Loop 3: New Iteration (select vote records)");
        // choose element
        state.leftListRecord = state.loop2_leftList.get(state.idxLeft);
        state.rightListRecord = state.loop2_rightList.get(state.idxRight);
        return List.of(state.leftListRecord, state.rightListRecord);
    }

    public int getCompareCount() {
        return state.compareCount;
    }

    public int getSize() {
        return state.size;
    }

    public boolean restart() {
        this.state = new OrderByMergeSortVotingState(unorderedRecords);
        this.previousStates.clear();
        return true;
    }

    public boolean undo() {
        if (previousStates.isEmpty()) {
            return false;
        }
        this.state = previousStates.pull();
        return true;
    }
}
