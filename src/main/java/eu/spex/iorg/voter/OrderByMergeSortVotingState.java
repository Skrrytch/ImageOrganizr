package eu.spex.iorg.voter;

import java.util.ArrayList;
import java.util.List;

import eu.spex.iorg.model.FileVoteRecord;

public class OrderByMergeSortVotingState {

    public int size;

    public int glob_blockSize;

    public int loop1_leftStart;

    public int loop2_leftSize;

    public int loop2_rightSize;

    public int idxLeft;

    public int idxRight;
    public int idxList;

    public int compareCount = 0;

    public boolean loop1nextIteration = true;
    public boolean loop2nextIteration = true;
    public FileVoteRecord leftListRecord;
    public FileVoteRecord rightListRecord;

    public List<FileVoteRecord> loop2_leftList;
    public List<FileVoteRecord> loop2_rightList;

    public List<FileVoteRecord> orderedRecords;

    public OrderByMergeSortVotingState(List<FileVoteRecord> recordList) {
        this.size = recordList.size();
        this.glob_blockSize = 1;
        this.orderedRecords = new ArrayList<>(recordList);
    }

    public OrderByMergeSortVotingState copy() {
        OrderByMergeSortVotingState clone = new OrderByMergeSortVotingState(this.orderedRecords);
        clone.idxLeft = idxLeft;
        clone.idxRight = idxRight;
        clone.idxList = idxList;
        clone.glob_blockSize = glob_blockSize;
        clone.compareCount = compareCount;
        clone.loop1_leftStart = loop1_leftStart;
        clone.loop2_leftSize = loop2_leftSize;
        clone.loop2_leftList = new ArrayList<>(loop2_leftList);
        clone.loop2_rightSize = loop2_rightSize;
        clone.loop2_rightList = new ArrayList<>(loop2_rightList);
        clone.rightListRecord = rightListRecord;
        clone.leftListRecord = leftListRecord;
        clone.loop1nextIteration = loop1nextIteration;
        clone.loop2nextIteration = loop2nextIteration;
        return clone;
    }
}
