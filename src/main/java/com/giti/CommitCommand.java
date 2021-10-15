package com.giti;

import exception.GitiException;
import picocli.CommandLine;

@CommandLine.Command(name = "commit")
public class CommitCommand implements Runnable {

    @CommandLine.Option(names = "--bare", description = "Treat the repository as a bare repository.")
    boolean bareRepoOption;

    @Override
    public void run() {
        try {
            commit(bareRepoOption);
        } catch (GitiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CommandLine(new CommitCommand()).execute(args);
    }

    public void commit(boolean bareRepoOption) throws GitiException {
    }

}
