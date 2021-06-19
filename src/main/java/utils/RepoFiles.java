package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static java.util.Objects.isNull;

public final class RepoFiles {
    private RepoFiles() {
    }

    private static Boolean inRepo;
    public static String gitiPath = System.getProperty("user.dir");

    public static boolean inGitiRepo() {
        if (isNull(inRepo)) {
            gitiPath = getGitiPath("");
            inRepo = !isNull(gitiPath);
        }
        return inRepo;
    }

    public static String getGitiPath(String dir) {
        try {
            Path p = Paths.get(System.getProperty("user.dir"));
            Path realPath = p;
            //            final String userDir = dir.isEmpty() ? System.getProperty("user.dir") : dir;
            boolean realFile = Paths.get(dir).toFile().exists();
            if (!realFile) {
                realPath = Paths.get(gitiPath, dir);
            }
            String path = realPath.toString();
            if (dir.equalsIgnoreCase(gitiPath)) {
                path = gitiPath;
            }
            if (Files.isDirectory(realPath) || Files.exists(realPath)) {
                File potentialConfigFile = new File(path, "config");
                File potentialGitiPath = new File(path, ".giti");
                File otherFile = new File(path);
                String regex = "\"core\"";
                Pattern pattern = Pattern.compile(regex);

                if (potentialConfigFile.exists()
                        && potentialConfigFile.isFile()
                        && Files.lines(potentialConfigFile.toPath())
                        .anyMatch(s -> pattern.matcher(s).find())) {
                    return potentialConfigFile.toString();
                } else if (otherFile.exists() && otherFile.isFile()) {
                    return otherFile.toString();
                } else if (potentialGitiPath.isDirectory()) {
                    return potentialGitiPath.toString();
                } else if (!path.equals("/") && !path.contains(":\\")) {
                    return getGitiPath(Paths.get(path, "..").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void assertInRepo() {
        if (!inGitiRepo()) {
            try {
                throw new Exception("not a Pgit repository");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String read(Path path) {
        if (path.toFile().exists()) {
            try (BufferedReader fileReader = new BufferedReader(new FileReader(path.toFile()))) {
                return fileReader.lines().collect(Collectors.joining());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static JsonObject initRepoStructure(boolean bareRepoOption) {
        JsonObject bareRepoStructure = new JsonObject();
        JsonObject repoStructure = new JsonObject();
        bareRepoStructure.addProperty("HEAD", "ref: refs/heads/master");
        JsonObject config = new JsonObject();
        JsonObject bare = new JsonObject();
        JsonArray core = new JsonArray();
        bare.addProperty("bare", bareRepoOption);
        bare.addProperty("repositoryformatversion", 0);
        core.add(bare);
        config.add("core", bare);
        JsonObject heads = new JsonObject();
        bareRepoStructure.add("config", config);
        heads.add("heads", new JsonObject());
        bareRepoStructure.add("objects", new JsonObject());
        bareRepoStructure.add("refs", heads);
        repoStructure.add(".giti", bareRepoStructure);
        return repoStructure;
    }

    public static JsonObject initRepoMetaData() {
        JsonObject jsonElement = new JsonObject();
        jsonElement.addProperty("HEAD", "file");
        jsonElement.addProperty("config", "file");
        for (String s : Arrays.asList("refs", "heads", "objects", ".giti")) {
            jsonElement.addProperty(s, "directory");
        }
        return jsonElement;
    }

    public static Path pathFromRoot(String path) {
        Path absolutePath = workingCopyPath("");
        String repoRoot = System.getProperty("user.dir");
        return absolutePath.relativize(Paths.get(repoRoot, path));
    }

    private static Path workingCopyPath(String path) {
        return Paths.get(Paths.get(RepoFiles.getGitiPath(""), "..").toString(), path);
    }

    public static boolean hasFile(Path path, int stage) {
        return false;
    }

    public static void readIndexFile() {
        String indexFilePath = getGitiPath("index");

    }
}
