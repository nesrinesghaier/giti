package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public  class ObjectsUtils {

    public static void writeObjectFile(String objectName, String fileContent) {
        Path path = Paths.get(RepoFiles.gitiDirPath, "objects", objectName);
        try (PrintWriter printWriter = new PrintWriter(new FileOutputStream(path.toFile()))) {
            File f = new File(path.toString());
            if (f.createNewFile()) {
                printWriter.write(fileContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}