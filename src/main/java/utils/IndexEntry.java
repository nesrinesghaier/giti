package utils;

import java.io.IOException;
import java.io.Serializable;

public class IndexEntry implements Serializable {
    String filePath;
    int contentSize;
    int stage;

    public IndexEntry(String filePath, int stage, int contentSize) {
        this.filePath = filePath;
        this.contentSize = contentSize;
        this.stage = stage;
    }

    @Override
    public String toString() {
        return filePath + " " + stage + " " + contentSize;
    }
}
