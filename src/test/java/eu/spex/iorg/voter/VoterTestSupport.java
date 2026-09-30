package eu.spex.iorg.voter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Vote;

/**
 * Simulates a user who always picks the image with the lower number ("img-03.jpg" beats "img-07.jpg").
 */
final class VoterTestSupport {

    private VoterTestSupport() {
    }

    /** Files img-01.jpg ... img-NN.jpg in a reproducible random order; the files do not need to exist. */
    static List<File> shuffledFiles(int count, long seed) {
        List<File> files = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            files.add(new File(String.format("/photos/img-%02d.jpg", i)));
        }
        Collections.shuffle(files, new Random(seed));
        return files;
    }

    static int rank(FileVoteRecord record) {
        String name = record.getFileName();
        return Integer.parseInt(name.substring(name.indexOf('-') + 1, name.indexOf('.')));
    }

    static FileVoteRecord better(Vote vote) {
        return rank(vote.getRecord1()) <= rank(vote.getRecord2()) ? vote.getRecord1() : vote.getRecord2();
    }

    static FileVoteRecord worse(Vote vote) {
        return better(vote) == vote.getRecord1() ? vote.getRecord2() : vote.getRecord1();
    }

    /** Votes until the voter is finished and returns the number of votes. */
    static int voteToTheEnd(Voter voter, Vote vote) {
        int votes = 0;
        while (vote != null) {
            vote = voter.vote(better(vote), "1");
            votes++;
        }
        return votes;
    }
}
