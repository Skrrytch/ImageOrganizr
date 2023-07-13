package eu.spex.iorg.voter;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import eu.spex.iorg.model.FileRename;
import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;
import eu.spex.iorg.model.VoteResult;
import eu.spex.iorg.service.Logger;

public class OrderByMergeSortVoter extends Voter {

    private OrderByMergeSortVoteResult voteResult;
    private int size;

    private int glob_blockSize;

    private int loop1_leftStart;

    private int loop2_leftSize;

    private int loop2_rightSize;

    private int idxLeft;

    private int idxRight;
    private int idxList;

    private int compareCount = 0;

    private boolean loop1nextIteration = true;
    private boolean loop2nextIteration = true;
    private FileVoteRecord leftListRecord;
    private FileVoteRecord rightListRecord;

    private List<FileVoteRecord> loop2_leftList;
    private List<FileVoteRecord> loop2_rightList;

    public OrderByMergeSortVoter(Mode mode) {
        super(mode);
        voteResult = new OrderByMergeSortVoteResult(mode);
    }

    @Override
    public boolean initCollection(List<File> files) {
        if (files.isEmpty()) {
            System.out.println("Im Verzeichnis wurden keine Bilder gefunden.");
            return false;
        }
        if (files.size() == 1) {
            System.out.println("Im Verzeichnis wurde nur ein Bild gefunden.");
            return false;
        }

        List<File> sortedFiles = files.stream().sorted(Comparator.comparing(File::getAbsolutePath)).collect(Collectors.toList());
        voteResult.setFiles(sortedFiles);

        size = files.size();
        glob_blockSize = 1;
        return true;
    }

    @Override
    public Vote vote(FileVoteRecord record, String voteValue) {
        compareCount++;
        record.vote();

        Logger.info("Loop 3: Voted. Updating indexes");
        // Finished inner loop after vote: while (i < leftSize && j < rightSize) ...
        if (record == leftListRecord) {
            voteResult.setRecord(idxList, loop2_leftList.get(idxLeft));
            idxLeft++;
        } else if (record == rightListRecord) {
            voteResult.setRecord(idxList, loop2_rightList.get(idxRight));
            idxRight++;
        } else {
            throw new IllegalArgumentException("Unexpected record.");
        }
        voteResult.onVoted(record);
        idxList++;
        // Finished all loops

        // After loop: while (i < leftSize && j < rightSize) ...
        // which is the end of loop: while (leftStart < n - blockSize) ...
        if (idxLeft >= loop2_leftSize || idxRight >= loop2_rightSize) {
            Logger.info("Loop 3: Ended. Finishing iteration of loop 2");
            while (idxLeft < loop2_leftSize) {
                voteResult.setRecord(idxList, loop2_leftList.get(idxLeft));
                idxLeft++;
                idxList++;
            }

            while (idxRight < loop2_rightSize) {
                voteResult.setRecord(idxList, loop2_rightList.get(idxRight));
                idxRight++;
                idxList++;
            }

            loop1_leftStart += 2 * glob_blockSize;
            loop2nextIteration = true;
        }

        // After loop: while (leftStart < n - blockSize) ...
        // which is the end of loop: while (blockSize < n) ...
        if (loop1_leftStart >= size - glob_blockSize) {
            Logger.info("Loop 2: Ended. Finishing iteration of loop 1");
            glob_blockSize *= 2;
            loop1nextIteration = true;
        }
        return getNextVote();
    }

    @Override
    public Vote getStartVote() {
        return getNextVote();
    }

    private Vote getNextVote() {
        // First loop (end criterium): while (blockSize < n)...
        if (glob_blockSize >= size) {
            Logger.info("Main loop ends");
            int idx = 1;
            for (FileVoteRecord record : voteResult.getUnorderedRecords()) {
                Logger.info("{0}. {1} (count: {2})", idx, record.getFileName(), record.getCountOfVotes());
                idx++;
            }
            return null;
        }
        // New iteration of loop 1: while (blockSize < n)...
        if (loop1nextIteration) {
            loop1nextIteration = false;
            Logger.info("Loop 1: New iteration");
            loop1_leftStart = 0;
        }

        // Next iteration of loop 2: while (leftStart < n - blockSize) ...
        if (loop2nextIteration) {
            loop2nextIteration = false;
            Logger.info("Loop 2: New iteration");
            int loop2_mid = loop1_leftStart + glob_blockSize - 1;
            int loop2_rightEnd = Math.min(loop1_leftStart + 2 * glob_blockSize - 1, size - 1);

            loop2_leftSize = loop2_mid - loop1_leftStart + 1;
            loop2_leftList = new ArrayList<>();
            for (int i = 0; i < loop2_leftSize; i++) {
                loop2_leftList.add(voteResult.getRecord(loop1_leftStart + i));
            }

            loop2_rightSize = loop2_rightEnd - loop2_mid;
            loop2_rightList = new ArrayList<>();
            for (int i = 0; i < loop2_rightSize; i++) {
                loop2_rightList.add(voteResult.getRecord(loop2_mid + i + 1));
            }

            idxLeft = 0;
            idxRight = 0;
            idxList = loop1_leftStart;
        }

        Logger.info("Loop 3: New Iteration (select vote records)");
        // choose element
        leftListRecord = loop2_leftList.get(idxLeft);
        rightListRecord = loop2_rightList.get(idxRight);
        Vote vote = new Vote(getStageDescription(), leftListRecord, rightListRecord);

        return vote;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public VoteResult getVoteResult() {
        return voteResult;
    }

    @Override
    public List<FileVoteRecord> generateFinalVoteResult() {
        List<FileVoteRecord> orderedRecords = getVoteResult().getOrderedRecords();
        int orderNr = 1;
        for (FileVoteRecord record : orderedRecords) {
            record.setFinalResult(
                    String.valueOf(orderNr),
                    generateNewFilepath(record, orderNr));
            orderNr++;
        }
        return orderedRecords;
    }

    private FileRename generateNewFilepath(FileVoteRecord voteRecord, int voteValue) {
        FileRename fileRename = new FileRename(voteRecord.getFilePath(), voteRecord.getFileName());
        fileRename.setNewFilename(MessageFormat.format("{0,number,000}-{1}", voteValue, voteRecord.getFileName()));
        return fileRename;
    }

    private String getStageDescription() {
        final String message = "{0} compared";
        return MessageFormat.format(message, this.compareCount);
    }
}
