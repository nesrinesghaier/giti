package utils;

import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RepoFiles {
    private RepoFiles() {
    }


    public static boolean inRepo() {
        return getPGitPath("") != null;
    }

    public static String getPGitPath(String dir) {
        try {
            Path realPath = Paths.get(dir).toAbsolutePath().normalize();
            String path = realPath.toString();
            if (Files.isDirectory(realPath) || Files.exists(realPath)) {
                File potentialConfigFile = new File(path, "config");
                File potentialPGitPath = new File(path, ".pgit");
                String regex = "\\[core]";
                Pattern pattern = Pattern.compile(regex);

                if (potentialConfigFile.exists()
                        && potentialConfigFile.isFile()
                        && java.nio.file.Files.lines(potentialConfigFile.toPath()).anyMatch(s -> pattern.matcher(s).find())) {
                    return path;
                } else if (potentialPGitPath.isDirectory()) {
                    return potentialPGitPath.toString();
                } else if (!path.equals("/")) {
                    return getPGitPath(Paths.get(path, "..").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void assertInRepo() {
        if (!inRepo()) try {
            throw new Exception("not a Pgit repository");
        } catch (Exception e) {
            e.printStackTrace();
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

    public static JSONObject initRepoStructure(boolean bareRepoOption) {
        JSONObject bareRepoStructure = new JSONObject();
        JSONObject repoStructure = new JSONObject();
        bareRepoStructure.put("HEAD", "ref: refs/heads/master\n");
        JSONObject config = new JSONObject();
        JSONObject bare = new JSONObject();
        JSONObject heads = new JSONObject();
        bare.put("", "bare = " + bareRepoOption + "\n");
        config.put("core", bare);
        bareRepoStructure.put("config", "[core]\n bare = " + bareRepoOption + "\n");
        heads.put("heads", new JSONObject());
        bareRepoStructure.put("objects", new JSONObject());
        bareRepoStructure.put("refs", heads);
        repoStructure.put(".pgit", bareRepoStructure);
        return repoStructure;
    }
}
