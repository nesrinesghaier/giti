package com.pgit;


import picocli.CommandLine;
import utils.Config;
import utils.RepoFiles;

@CommandLine.Command(name = "add")
public class AddCommand implements Runnable {
    @Override
    public void run() {
        try {
            add("config");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void add(String path) throws Exception {
        if (!RepoFiles.inRepo()) {
            throw new Exception("not a pGit repository");
        }
        Config.read();
    }

    public static void main(String[] args) {
        new CommandLine(new AddCommand()).execute(args);
    }

}
