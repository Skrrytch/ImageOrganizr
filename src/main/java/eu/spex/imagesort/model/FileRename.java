package eu.spex.imagesort.model;

public class FileRename {
    private final String originalPath;

    private final String originalFilename;

    private String newDirectory;

    private String newFilename;

    public FileRename(String originalPath, String originalFilename) {
        this.originalPath = originalPath;
        this.originalFilename = originalFilename;
    }

    public void setNewDirectory(String newDirectory) {
        this.newDirectory = newDirectory;
    }

    public void setNewFilename(String newFilename) {
        this.newFilename = newFilename;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getNewDirectory() {
        return newDirectory;
    }

    public String getNewFilename() {
        return newFilename;
    }
}
