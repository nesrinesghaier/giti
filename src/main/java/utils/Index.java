package utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.HashMap;

public final class Index {

    public static String key(String path, String stage) {
        return path + "," + stage;
    }

    public static boolean hasFile(String path, String stage) throws IOException {
        try {
            final File index = Paths.get(RepoFiles.gitiPath, "index").toFile();
            if (index.exists()) {
                FileInputStream fis = new FileInputStream(index);
                ObjectInputStream ois = new ObjectInputStream(fis);
                HashMap<String, String> map = (HashMap<String, String>) ois.readObject();
                return map.containsKey(key(path, stage));
            }
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFileConflict(String path) {
        return false;
    }

    public static void writeNonConflict(String path, String content) {
        final File index = Paths.get(RepoFiles.gitiPath, "index").toFile();
        if (index.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(index);
                ObjectOutputStream ous = new ObjectOutputStream(fileOutputStream);
                ous.writeObject(content);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
