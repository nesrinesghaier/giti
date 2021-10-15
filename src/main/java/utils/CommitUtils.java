package utils;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommitUtils {
    static String separator = Pattern.quote(File.separator);


    public static JsonObject getJsonObjectFromPathArray(List<String> path, int index) {
        JsonObject jsonObject = new JsonObject();
        if (index == path.size()) return jsonObject;
        if (path.get(index).isEmpty()) {
            jsonObject.add("/", getJsonObjectFromPathArray(path, index + 1));
        } else if (index == path.size() - 2) {
            jsonObject.addProperty(path.get(index), path.get(path.size() - 1));
        } else {
            jsonObject.add(path.get(index), getJsonObjectFromPathArray(path, index + 1));
        }
        return jsonObject;
    }

    public static void writeObjectsTree() {
        List<List<String>> indexPaths = getIndexPaths();
        for (List<String> indexPath : indexPaths) {
            JsonObject fromPathArray = getJsonObjectFromPathArray(indexPath, 0);
            WriteJsonObjectTree(fromPathArray);
        }


    }

    //parse json tree and hash every value of a key and create a file of it in objects
    private static void WriteJsonObjectTree(JsonObject fromPathArray) {
    }


    private static List<List<String>> getIndexPaths() {
        try (Stream<String> stream = Files.lines(IndexUtils.indexFile.toPath())) {
            return stream.map(CommitUtils::splitLine).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<String> splitLine(String line) {
        String[] lineDetails = line.split(" ");
        List<String> list = new LinkedList<>(Arrays.asList(lineDetails[0].split(separator)));
        list.add(lineDetails[2]);
        return list;
    }


    public static void main(String[] args) {
        writeObjectsTree();
    }


}
