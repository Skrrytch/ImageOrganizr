package eu.spex.iorg.voter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;
import eu.spex.iorg.model.Vote;

class CategorizeVoterTest {

    private static final List<File> FILES = List.of(new File("/photos/a.jpg"), new File("/photos/b.jpg"));

    @Test
    void ratingIsPrefixedAsTwoDigitNumber() {
        CategorizeVoter voter = new CategorizeVoter(Mode.RATE);
        voter.initCollection(FILES);

        Vote vote = voter.getStartVote();
        FileVoteRecord a = vote.getRecord1();
        vote = voter.vote(a, "7");
        FileVoteRecord b = vote.getRecord1();
        assertNull(voter.vote(b, "10"));

        assertEquals("07-a.jpg", a.getFinalFileRename().getNewFilename());
        assertEquals("10-b.jpg", b.getFinalFileRename().getNewFilename());
    }

    @Test
    void categoryBecomesTheSubdirectory() {
        CategorizeVoter voter = new CategorizeVoter(Mode.CATEGORIZE);
        voter.initCollection(FILES);

        FileVoteRecord a = voter.getStartVote().getRecord1();
        voter.vote(a, "family");

        assertEquals("family", a.getFinalFileRename().getNewDirectory());
        assertEquals("a.jpg", a.getFinalFileRename().getNewFilename());
        assertEquals(List.of(a), voter.checkVote(null, "family").getPreviewRecords());
    }

    @Test
    void undoRemovesTheImageFromItsCategory() {
        CategorizeVoter voter = new CategorizeVoter(Mode.CATEGORIZE);
        voter.initCollection(FILES);

        FileVoteRecord a = voter.getStartVote().getRecord1();
        voter.vote(a, "family");
        Vote again = voter.undo();

        assertEquals(a, again.getRecord1());
        assertEquals(List.of(), voter.checkVote(null, "family").getPreviewRecords());
    }
}
