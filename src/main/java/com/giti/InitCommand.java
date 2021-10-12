package com.giti;

import com.google.gson.JsonObject;
import exception.GitiException;
import picocli.CommandLine;
import utils.RepoFiles;

import static java.lang.System.getProperty;
import static java.lang.System.out;

@CommandLine.Command(name = "init")
public class InitCommand implements Runnable {

    @CommandLine.Option(names = "--bare", description = "Treat the repository as a bare repository.")
    boolean bareRepoOption;

    @Override
    public void run() {
        try {
            init(bareRepoOption);
        } catch (GitiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CommandLine(new InitCommand()).execute(args);
    }

    public void init(boolean bareRepoOption) throws GitiException {
        if (RepoFiles.inGitiRepo()) {
            out.println(".giti dir already exists");
            return;
        }
        JsonObject repoStructure = RepoFiles.initRepoStructure(bareRepoOption);
        JsonObject metaData = RepoFiles.initRepoMetaData();
        RepoFiles.writeFilesFromTree(bareRepoOption ? repoStructure.getAsJsonObject(".giti") : repoStructure,
                getProperty("user.dir"), metaData);
        out.println("Initialized empty Giti repository");
    }

}
