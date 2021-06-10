package utils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.file.Paths;

public final class Config {
    public static boolean isBare() {
        return false;
    }

    public static boolean read() {
        String configPath = RepoFiles.getPGitPath("config");
        assert configPath != null;
        String configJson = RepoFiles.read(Paths.get(configPath));
        JSONParser jsonParser = new JSONParser();
        try {
            Object parse = jsonParser.parse(configJson);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

}
