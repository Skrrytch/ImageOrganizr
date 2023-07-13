package eu.spex.iorg.model;

public class Vote {

    private final String stageDescription;

    private final FileVoteRecord file1;

    private final FileVoteRecord file2;

    public Vote(String stageDescription, FileVoteRecord file1, FileVoteRecord file2) {
        this.stageDescription = stageDescription;
        this.file1 = file1;
        this.file2 = file2;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    public FileVoteRecord getRecord1() {
        return file1;
    }

    public FileVoteRecord getRecord2() {
        return file2;
    }

    public void countVoting() {
        if (file1 != null) {
            file1.countVoting();
        }
        if (file2 != null) {
            file2.countVoting();
        }
    }
}
