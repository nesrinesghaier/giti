package com.pgit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import picocli.CommandLine;
import utils.Config;
import utils.Index;
import utils.RepoFiles;

import static java.util.Objects.isNull;

@CommandLine.Command(name = "add")
public class AddCommand implements Runnable {
    private static String GIT_REPO_NAME = ".git";

    @CommandLine.Parameters(paramLabel = "<files>", description = "the file to add")
    List<File> files = new ArrayList<>();

    @Override
    public void run() {
        try {
            add(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void add(List<File> files) throws Exception {
        if (!RepoFiles.inGitiRepo()) {
            throw new Exception("not a Giti repository");
        }
        Config.assertNotBare();
        HashMap<String, Boolean> options = new HashMap<>();
        options.put("add", true);

        if (!files.isEmpty()) {
            files.forEach(f -> updateIndex(f, options));
        } else {
            List<File> addedFiles = new ArrayList<>();
            final String currentPath = System.getProperty("user.dir");

            lsRecursive(currentPath, addedFiles);
            if (addedFiles.isEmpty()) {
                throw new Exception(RepoFiles.pathFromRoot("") + " did not match any files");
            } else {
                System.out.println(addedFiles.size());
                final String index = RepoFiles.getGitiPath("index");
                if (isNull(index)) {
                    if (Paths.get(RepoFiles.gitiPath, "index").toFile().createNewFile()) {
                        addedFiles.forEach(f -> updateIndex(f, options));
                    }
                } else {

                }
            }
        }
    }

    public static void lsRecursive(String path, List<File> files) {
        File f = new File(path);
        if (f.isFile()) {
            files.add(f);
        } else if (f.isDirectory() && !f.getName().contains(GIT_REPO_NAME)) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                lsRecursive(file.getAbsolutePath(), files);
            }
        }
    }

    public void updateIndex(File f, Map<String, Boolean> ops) {
        try {
            RepoFiles.assertInRepo();
            Config.assertNotBare();
            //            Map<String, Boolean> options = !ops.isEmpty() ? ops : new HashMap<>();
            final Path pathFromRoot = RepoFiles.pathFromRoot(f.toString());
            boolean fileExists = f.exists();
            boolean isInIndex = Index.hasFile(f.toString(), "0");
            if (fileExists && f.isDirectory()) {
                throw new Exception(pathFromRoot + " is a directory - add files inside\n");
            } else if (ops.get("remove") && !fileExists && isInIndex) {
                if (Index.isFileConflict(f.toString())) {
                    throw new Exception("unsupported");
                } else {
                    // remove the file from index
                    return;
                }
            } else if (ops.get("remove") && !fileExists && !isInIndex) {
                return;
            } else if (!ops.get("add") && fileExists && !isInIndex) {
                throw new Exception("cannot add " + pathFromRoot + " to index - use --add option\n");
            } else if (fileExists && (ops.get("add") || isInIndex)) {
                //                index.writeNonConflict(path, files.read(files.workingCopyPath(path)));
                return;
            } else if (!ops.get("remove") && !fileExists) {
                throw new Exception(pathFromRoot + " does not exist and --remove not passed\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CommandLine(new AddCommand()).execute(args);
    }

}
