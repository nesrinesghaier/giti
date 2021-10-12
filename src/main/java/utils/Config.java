package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import exception.GitiException;

import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

public final class Config {
    private static Boolean isBareRepo;

    public static void assertNotBare() throws GitiException {
        if (isBareRepo != null) return;
        isBareRepo = readBareCheckFromConfig();
        if (isBareRepo) {
            throw new GitiException("this operation must be run in a work tree");
        }
    }

    private Config() {
    }

    //read bare value from config file
    public static boolean readBareCheckFromConfig() throws GitiException {
        String gitiPath = RepoFiles.gitiDirPath;
        try {
            String configPath = RepoFiles.getFullPathIfExists(Paths.get(gitiPath), false, "config");
            String configString = RepoFiles.read(Paths.get(requireNonNull(configPath)));
            if (configString != null) {
                JsonElement je = JsonParser.parseString(configString);
                return je.getAsJsonObject().get("core").getAsJsonObject().get("bare").getAsBoolean();
            }
        } catch (GitiException e) {
            throw new GitiException("This operation must be run in a work-tree");
        }
        return false;
    }

}
