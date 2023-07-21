package eu.spex.iorg.voter;

import java.io.File;
import java.text.MessageFormat;
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

    private final OrderByMergeSortVoteResult voteResult;


    public OrderByMergeSortVoter(Mode mode) {
        super(mode);
        voteResult = new OrderByMergeSortVoteResult(mode);
    }

    @Override
    public boolean initCollection(List<File> files) {
        if (files.isEmpty()) {
            Logger.warn("Im Verzeichnis wurden keine Bilder gefunden.");
            return false;
        }
        if (files.size() == 1) {
            Logger.warn("Im Verzeichnis wurde nur ein Bild gefunden.");
            return false;
        }

        List<File> sortedFiles = files.stream().sorted(Comparator.comparing(File::getAbsolutePath)).collect(Collectors.toList());
        voteResult.setFiles(sortedFiles);
        return true;
    }

    @Override
    public Vote vote(FileVoteRecord record, String voteValue) {
        List<FileVoteRecord> nextVoteRecord = voteResult.vote(record, voteValue);
        if (nextVoteRecord != null) {
            return new Vote(getStageDescription(), nextVoteRecord.get(0), nextVoteRecord.get(1));
        } else {
            return null; // finished
        }
    }


    @Override
    public Vote getStartVote() {
        return getNextVote();
    }

    private Vote getNextVote() {
        List<FileVoteRecord> nextVoteRecord = voteResult.getNextVote();
        if (nextVoteRecord != null) {
            return new Vote(getStageDescription(), nextVoteRecord.get(0), nextVoteRecord.get(1));
        } else {
            return null; // finished
        }
    }

    @Override
    public int getSize() {
        return voteResult.getSize();
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
        return MessageFormat.format(message, voteResult.getCompareCount());
    }

    @Override
    public boolean supportsUndo() {
        return true;
    }

    @Override
    public Vote undo() {
        if (voteResult.undo()) {
            return getNextVote();
        } else {
            Logger.warn("Undo not possible");
            return getNextVote();
        }
    }

    @Override
    public boolean supportsRestart() {
        return true;
    }

    @Override
    public Vote restart() {
        if (voteResult.restart()) {
            return getStartVote();
        } else {
            Logger.warn("Undo not possible");
            return getStartVote();
        }
    }
}
