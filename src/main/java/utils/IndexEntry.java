package utils;

import java.io.Serializable;

public class IndexEntry implements Serializable {
    String filePath;
    String hashedContent;
    int stage;

    public IndexEntry(String filePath, int stage, String contentSize) {
        this.filePath = filePath;
        this.hashedContent = contentSize;
        this.stage = stage;
    }

    @Override
    public String toString() {
        return filePath + " " + stage + " " + hashedContent;
    }
}
