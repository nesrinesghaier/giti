package utils;

import com.github.underscore.lodash.U;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.GitiException;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public final class RepoFiles {
    private RepoFiles() {
    }

    public static String gitiDirPath;
    private static Boolean gitiDirExists;
    public static final String GITI = ".giti";
    public static final String CURRENT_DIR = "user.dir";
    public static final String GITI_PATH = System.getProperty(CURRENT_DIR);

    // check if .giti directory exists
    public static boolean inGitiRepo() throws GitiException {
        gitiDirPath = getFullPathIfExists(Paths.get(GITI_PATH), true, GITI);
        if (isNull(gitiDirExists)) gitiDirExists = !isNull(gitiDirPath);
        return gitiDirExists;
    }

    public static String getFullPathIfExists(Path searchDir, boolean isDirectory, String pathName) throws GitiException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchDir)) {
            for (Path path : stream) {
                if (isDirectory) {
                    if (Files.isDirectory(path) && path.getFileName().toString().equals(pathName)) {
                        return path.toString();
                    }
                } else if (path.getFileName().toString().equals(pathName)) {
                    return path.toString();
                }
            }
            if (searchDir.getParent() != null && isDirectory) {
                return getFullPathIfExists(searchDir.getParent(), isDirectory, pathName);
            }
        } catch (IOException e) {
            throw new GitiException(e.getMessage());
        }
        return null;
    }

    public static String getGitiPath(String dir) {
        try {
            Path p = Paths.get(System.getProperty("user.dir"));
            Path realPath = p;
            //            final String userDir = dir.isEmpty() ? System.getProperty("user.dir") : dir;
            boolean realFile = Paths.get(dir).toFile().exists();
            if (!realFile) {
                realPath = Paths.get(GITI_PATH, dir);
            }
            String path = realPath.toString();
            if (dir.equalsIgnoreCase(GITI_PATH)) {
                path = GITI_PATH;
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

    public static void assertInRepo() throws GitiException {
        if (!inGitiRepo()) {
            throw new GitiException("not a Giti repository");
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
        String currentPath = System.getProperty("user.dir");
        return absolutePath.relativize(Paths.get(currentPath, path));
    }

    private static Path workingCopyPath(String path) {
        return Paths.get(Paths.get(RepoFiles.getGitiPath(""), "..").toString(), path);
    }

    public static void writeFilesFromTree(JsonObject structure, String prefix, JsonObject metadata) {
        for (Map.Entry<String, JsonElement> entry : structure.entrySet()) {
            Path path = Paths.get(prefix, entry.getKey());
            if (isFile(metadata, entry)) {
                writeConfigFileIfAbsent(entry, path);
            } else {
                createGitiDirectory(metadata, entry, path);
            }
        }
    }

    private static boolean isFile(JsonObject metadata, Map.Entry<String, JsonElement> entry) {
        return metadata.get(entry.getKey()).getAsString().equalsIgnoreCase("file");
    }

    private static void writeConfigFileIfAbsent(Map.Entry<String, JsonElement> entry, Path path) {
        File fileToBeCreated = new File(path.toString());
        try (FileWriter fileWriter = new FileWriter(fileToBeCreated, true)) {
            if (fileToBeCreated.exists()) {
                if (entry.getValue().isJsonPrimitive()) {
                    String value = entry.getValue().getAsString();
                    fileWriter.write(value);
                } else {
                    fileWriter.write(U.formatJson(entry.getValue().toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createGitiDirectory(JsonObject metadata, Map.Entry<String, JsonElement> entry, Path path) {
        try {
            if (SystemConfig.isPosix) {
                Files.createDirectory(path,
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
            } else {
                Files.createDirectory(path);
            }
            if (entry.getValue().isJsonObject()) {
                writeFilesFromTree(entry.getValue().getAsJsonObject(), path.toString(), metadata);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
