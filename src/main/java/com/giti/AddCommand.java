package com.giti;

import exception.GitiException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import utils.Config;
import utils.RepoFiles;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static utils.IndexUtils.*;

@CommandLine.Command(name = "add")
@Slf4j
public class AddCommand implements Runnable {

    public static final String GITI = ".giti";
    private static Set<File> filesToIndex = new HashSet<>();

    @CommandLine.Parameters(paramLabel = "<files>", description = "Files to be added")
    Set<File> files = new HashSet<>();

    @Override
    public void run() {
        try {
            add(files);
        } catch (GitiException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void add(Set<File> files) throws GitiException, IOException {
        if (!RepoFiles.inGitiRepo()) {
            throw new GitiException("Not a Giti repository");
        }
        Config.assertNotBare();
        if (!files.isEmpty()) {
            files.forEach(f -> filesToIndex.addAll(lsRecursive(f.toString())));
            filesToIndex.forEach(AddCommand::updateIndex);
        } else {
            final String currentPath = RepoFiles.GITI_PATH;
            filesToIndex = lsRecursive(currentPath);

            if (filesToIndex.isEmpty()) {
                throw new GitiException(RepoFiles.pathFromRoot("") + " did not match any files");
            }
            // creates the index config file if it doesn't exist
            indexFile.createNewFile();
            filesToIndex.forEach(AddCommand::updateIndex);
        }
    }

    public static void updateIndex(File f) {
        try {
            boolean fileExists = f.exists();
            if (fileExists && f.isDirectory()) {
                log.info(f + " is a directory - add files inside\n");
            } else if (fileExists) {
                writeNonConflict(f);
            } else {
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
