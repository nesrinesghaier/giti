package utils;

import java.nio.file.Paths;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Config {
    public static boolean assertNotBare() throws Exception {
        if (readBareCheckFromConfig()){
            throw new Exception("this operation must be run in a work tree");
        }
        return false;
    }

    private Config() {
    }

    public static boolean readBareCheckFromConfig() {
        String configPath = RepoFiles.getPGitPath("");
        assert configPath != null;
        String configString = RepoFiles.read(Paths.get(configPath, "config"));
        if (configString != null) {
            JsonElement je = JsonParser.parseString(configString);
            return je.getAsJsonObject().get("core").getAsJsonObject().get("bare").getAsBoolean();
        }
        return false;
    }

}
