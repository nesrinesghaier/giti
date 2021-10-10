package com.giti;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import exception.GitiException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import utils.Config;
import utils.IndexUtils;
import utils.RepoFiles;

import static java.util.Objects.isNull;

@CommandLine.Command(name = "add")
@Slf4j
public class AddCommand implements Runnable {
    public static final String GITI = ".giti";

    @CommandLine.Parameters(paramLabel = "<files>", description = "the file to add")
    List<File> files = new ArrayList<>();

    @Override
    public void run() {
        try {
            add(files);
        } catch (GitiException | IOException e) {
            e.printStackTrace();
        }
    }

    private void add(List<File> files) throws GitiException, IOException {
        if (!RepoFiles.inGitiRepo()) {
            throw new GitiException("not a Giti repository");
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
                throw new GitiException(RepoFiles.pathFromRoot("") + " did not match any files");
            } else {
                final String index = RepoFiles.getFullPathIfExists(Paths.get(RepoFiles.gitiDirPath), false, "index");
                if (isNull(index)) {
                    if (Paths.get(RepoFiles.gitiDirPath, "index").toFile().createNewFile()) {
                        addedFiles.forEach(f -> updateIndex(f, options));
                    }
                } else {
                    addedFiles.forEach(f -> updateIndex(f, options));
                }
            }
        }
    }

    public static void lsRecursive(String path, List<File> files) {
        File f = new File(path);
        if (f.isFile()) {
            files.add(f);
        } else if (f.isDirectory() && !f.getName().contains(GITI)) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                lsRecursive(file.getAbsolutePath(), files);
            }
        }
    }

    public void updateIndex(File f, Map<String, Boolean> ops) {
        try {
            RepoFiles.assertInRepo();
            Config.assertNotBare();
            boolean fileExists = f.exists();
            boolean isInIndexFile = IndexUtils.isInIndexFile(f.toString(), 0);
            if (fileExists && f.isDirectory()) {
                throw new GitiException(f + " is a directory - add files inside\n");
            } else if (ops.get("remove") != null && !fileExists && isInIndexFile) {
                if (IndexUtils.isFileConflict(f.toString())) {
                    throw new GitiException("unsupported");
                } else {
                    // remove the file from index
                    return;
                }
            } else if (ops.get("remove") != null && !fileExists && !isInIndexFile) {
                return;
            } else if (ops.get("add") != null && fileExists && !isInIndexFile) {
                IndexUtils.writeNonConflict(f.toString());
                log.info("cannot add " + f + " to index - use --add option\n");
            } else if (fileExists && (ops.get("add") || isInIndexFile)) {
                return;
            } else if (ops.get("remove") != null && !fileExists) {
                throw new GitiException(f + " does not exist and --remove not passed\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CommandLine(new AddCommand()).execute(args);
    }

}
