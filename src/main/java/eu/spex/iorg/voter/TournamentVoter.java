package eu.spex.iorg.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import eu.spex.iorg.model.FileRename;
import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;
import eu.spex.iorg.model.VoteResult;

public class TournamentVoter extends Voter {

    private final TournamentVoteResult voteResult;
    private int size;

    public TournamentVoter(Mode mode) {
        super(mode);
        voteResult = new TournamentVoteResult(mode);
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
        return true;
    }

    @Override
    public Vote vote(FileVoteRecord record, String voteValue) {
        record.vote();
        voteResult.onVoted(record);
        return getNextVote();
    }

    @Override
    public Vote getStartVote() {
        return getNextVote();
    }

    private Vote getNextVote() {
        FileVoteRecord firstRecord = voteResult.getFirst();
        if (firstRecord == null) {
            return null; // fertig
        }
        FileVoteRecord secondRecord = voteResult.getSecond();
        Vote vote = new Vote(voteResult.getStageDescription(), firstRecord, secondRecord);
        vote.countVoting();
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
        List<FileVoteRecord> result = new ArrayList<>();
        int orderNr = 1;
        List<List<FileVoteRecord>> listsOfRecords = voteResult.getListsOfRecords();
        int listIdx = listsOfRecords.size() - 1;
        while (listIdx >= 0) {
            List<FileVoteRecord> recordList = listsOfRecords.get(listIdx);
            for (FileVoteRecord record : recordList) {
                record.setFinalResult(String.valueOf(orderNr), generateNewFilepath(record, orderNr));
                result.add(0, record); // add to the beginning for the correct order (best first)
            }
            orderNr += recordList.size();
            listIdx--;
        }
        return result;
    }

    private FileRename generateNewFilepath(FileVoteRecord voteRecord, int voteValue) {
        FileRename fileRename = new FileRename(voteRecord.getFilePath(), voteRecord.getFileName());
        fileRename.setNewFilename(mode.getParameter() + "-" + voteValue + "-" + voteRecord.getFileName());
        return fileRename;
    }
}
