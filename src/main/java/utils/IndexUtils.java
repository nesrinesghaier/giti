package utils;

import lombok.var;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class IndexUtils {

    public static final String INDEX_FILE = "index";

    public static boolean isInIndexFile(String path, int stage) throws IOException {
        final File index = Paths.get(RepoFiles.gitiDirPath, INDEX_FILE).toFile();
        try (FileInputStream fis = new FileInputStream(index);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {
            if (index.exists()) {
                return bufferedReader.lines().anyMatch(l -> l.contains(path + " " + stage));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFileConflict(String path) {
        return false;
    }

    public static void writeNonConflict(String path) {
        final File index = Paths.get(RepoFiles.gitiDirPath, "index").toFile();
        if (index.exists()) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(index, true);
                 PrintWriter printWriter = new PrintWriter(fileOutputStream)) {
                IndexEntry indexEntry = new IndexEntry(path, 0, path.getBytes().length);
                printWriter.println(indexEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void read() {
        final Path index = Paths.get(RepoFiles.GITI_PATH, "index");
        try (Stream<String> s = Files.lines(index)) {
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
