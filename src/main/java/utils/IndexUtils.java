package utils;

import exception.GitiException;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IndexUtils {

    private static final String INDEX_FILE_NAME = "index";
    private static final Set<File> filesToIndex = new HashSet<>();

    public static final File indexFile;

    static {
        if (RepoFiles.gitiDirPath == null) {
            try {
                RepoFiles.inGitiRepo();
            } catch (GitiException e) {
                e.printStackTrace();
            }
        }
        indexFile = Paths.get(RepoFiles.gitiDirPath, INDEX_FILE_NAME).toFile();
    }

    private IndexUtils() {
    }

    public static boolean isInIndexFile(String path, int stage) throws IOException {
        try (FileInputStream fis = new FileInputStream(indexFile);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {

            return bufferedReader.lines().anyMatch(l -> l.contains(path + " " + stage));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFileConflict(String path) {
        try (FileInputStream fis = new FileInputStream(indexFile);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {
            return bufferedReader.lines().anyMatch(l -> l.contains(path + " " + 2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void writeNonConflict(File file) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(indexFile, true);
             PrintWriter printWriter = new PrintWriter(fileOutputStream)) {
            boolean isInIndexFile = IndexUtils.isInIndexFile(file.getPath(), 0);
            if (isInIndexFile) {
                removeFileFromIndex(file);
            }
            String fileContent = readFile(file);
            String hashedContent = hashFileContent(fileContent);
            IndexEntry indexEntry = new IndexEntry(file.getPath(), 0, hashedContent);
            ObjectsUtils.writeObjectFile(hashedContent, fileContent);
            printWriter.println(indexEntry);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String hashFileContent(String fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(fileContent.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printBase64Binary(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    // read file content into string
    private static String readFile(File path) {
        try (Stream<String> stream = Files.lines(path.toPath())) {
            return stream.collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void removeFileFromIndex(File file) {
        try {
            List<String> readLines = FileUtils.readLines(indexFile, StandardCharsets.UTF_8);
            List<String> otherLines = getOtherLines(file, readLines);
            FileUtils.writeLines(indexFile, otherLines, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get all index file lines different from the file name to be removed
    private static List<String> getOtherLines(File file, List<String> lines) {
        return lines.stream()
                .filter(s -> !s.isEmpty() && !s.contains(file.getAbsolutePath()))
                .collect(Collectors.toList());
    }


    public static Set<File> lsRecursive(String path) {
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            filesToIndex.add(f.getAbsoluteFile());
        } else if (f.isDirectory() && !f.getName().equals(RepoFiles.GITI)) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                lsRecursive(file.getAbsolutePath());
            }
        }
        return filesToIndex;
    }
}
