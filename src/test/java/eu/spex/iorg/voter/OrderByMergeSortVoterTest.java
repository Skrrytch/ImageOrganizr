package eu.spex.iorg.voter;

import static eu.spex.iorg.voter.VoterTestSupport.better;
import static eu.spex.iorg.voter.VoterTestSupport.rank;
import static eu.spex.iorg.voter.VoterTestSupport.shuffledFiles;
import static eu.spex.iorg.voter.VoterTestSupport.voteToTheEnd;
import static eu.spex.iorg.voter.VoterTestSupport.worse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;

class OrderByMergeSortVoterTest {

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 7, 8, 9, 16, 17, 33})
    void findsTheCompleteOrder(int count) {
        OrderByMergeSortVoter voter = new OrderByMergeSortVoter(Mode.ORDER);
        assertTrue(voter.initCollection(shuffledFiles(count, count)));

        int votes = voteToTheEnd(voter, voter.getStartVote());

        List<FileVoteRecord> result = voter.generateFinalVoteResult();
        assertEquals(count, result.size());
        for (int i = 0; i < count; i++) {
            assertEquals(i + 1, rank(result.get(i)));
            assertEquals(String.format("%03d-img-%02d.jpg", i + 1, i + 1),
                    result.get(i).getFinalFileRename().getNewFilename());
        }
        int log2 = 32 - Integer.numberOfLeadingZeros(count - 1);
        assertTrue(votes <= count * log2, votes + " votes for " + count + " images");
    }

    @Test
    void undoRevertsAWrongVote() {
        OrderByMergeSortVoter voter = new OrderByMergeSortVoter(Mode.ORDER);
        voter.initCollection(shuffledFiles(6, 42));

        Vote first = voter.getStartVote();
        voter.vote(worse(first), "1");
        Vote again = voter.undo();

        assertEquals(first.getRecord1(), again.getRecord1());
        assertEquals(first.getRecord2(), again.getRecord2());
        voteToTheEnd(voter, again);
        List<FileVoteRecord> result = voter.generateFinalVoteResult();
        for (int i = 0; i < result.size(); i++) {
            assertEquals(i + 1, rank(result.get(i)));
        }
    }

    @Test
    void restartBeginsWithTheFirstComparison() {
        OrderByMergeSortVoter voter = new OrderByMergeSortVoter(Mode.ORDER);
        voter.initCollection(shuffledFiles(5, 7));
        Vote first = voter.getStartVote();
        voter.vote(better(first), "1");

        Vote restarted = voter.restart();

        assertEquals(first.getRecord1(), restarted.getRecord1());
        assertEquals(first.getRecord2(), restarted.getRecord2());
    }

    @Test
    void needsAtLeastTwoImages() {
        assertFalse(new OrderByMergeSortVoter(Mode.ORDER).initCollection(List.of()));
        assertFalse(new OrderByMergeSortVoter(Mode.ORDER).initCollection(List.of(new File("/photos/a.jpg"))));
    }
}
