package eu.spex.iorg.voter;

import static eu.spex.iorg.voter.VoterTestSupport.rank;
import static eu.spex.iorg.voter.VoterTestSupport.shuffledFiles;
import static eu.spex.iorg.voter.VoterTestSupport.voteToTheEnd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.spex.iorg.model.FileVoteRecord;
import eu.spex.iorg.model.Mode;

class TournamentVoterTest {

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 7, 8, 13, 16})
    void simpleKnockoutFindsTheWinner(int count) {
        List<FileVoteRecord> result = play(Mode.SIMPLE_KNOCKOUT, count);

        assertEquals(1, rank(result.get(0)));
        assertEquals("1", result.get(0).getFinalVoting());
        assertTrue(result.get(0).getFinalFileRename().getNewFilename().startsWith("001-"));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 7, 8, 13, 16})
    void fullKnockoutPlacesEveryImage(int count) {
        List<FileVoteRecord> result = play(Mode.FULL_KNOCKOUT, count);

        assertEquals(1, rank(result.get(0)));
        for (int i = 0; i < count; i++) {
            assertEquals(String.valueOf(i + 1), result.get(i).getFinalVoting());
        }
    }

    private static List<FileVoteRecord> play(Mode mode, int count) {
        TournamentVoter voter = new TournamentVoter(mode);
        assertTrue(voter.initCollection(shuffledFiles(count, count)));
        voteToTheEnd(voter, voter.getStartVote());
        List<FileVoteRecord> result = voter.generateFinalVoteResult();
        assertEquals(count, result.size());
        result.forEach(record -> assertNotNull(record.getFinalFileRename()));
        return result;
    }
}
