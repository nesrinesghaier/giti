package com.pgit;

import picocli.CommandLine;
import utils.RepoFiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

@CommandLine.Command(name = "init")
public class InitCommand implements Runnable {

    @CommandLine.Option(names = "--bare", description = "Treat the repository as a bare repository.")
    boolean bareRepoOption;

    @Override
    public void run() {
        init(bareRepoOption);
    }

    public static void main(String[] args) {
        new CommandLine(new InitCommand()).execute(args);
    }

    public void init(boolean bareRepoOption) {
        if (RepoFiles.inRepo()) {
            System.out.println("Already in a repo");
            return;
        }
        JSONObject repoStructure = RepoFiles.initRepoStructure(bareRepoOption);
        writeFilesFromTree(bareRepoOption ? (JSONObject) repoStructure.get(".pgit") : repoStructure, System.getProperty("user.dir"));
        System.out.println("Initialized empty Git repository");
    }

    void writeFilesFromTree(JSONObject structure, String prefix) {
        for (Object key : structure.keySet()) {
            Object value = structure.get(key);
            Path path = Paths.get(prefix, key.toString());
            if (value instanceof String) {
                try {
//                    String filePath = Paths.get(path.toString(), key.toString()).toString();
                    File newCreatedFile = new File(path.toString());
                    boolean fileCreated = newCreatedFile.createNewFile();
                    if (fileCreated) {
                        FileWriter fileWriter = new FileWriter(newCreatedFile);
                        fileWriter.write((String) value);
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!path.toFile().exists()) {
                    try {
                        Files.createDirectory(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                writeFilesFromTree((JSONObject) value, path.toString());
            }
        }
    }

    static class PGitStructure {
        String Head;
        Config config;

        public static class Config {
            HashMap<Object, Object> core;

            public Config(HashMap<Object, Object> core) {
                this.core = core;
            }
        }

        Object objects;
        HashMap<Object, Object> refs;

        public PGitStructure(String head, Config config, Object objects, HashMap<Object, Object> refs) {
            Head = head;
            this.config = config;
            this.objects = objects;
            this.refs = refs;
        }
    }

    private Path pathFromRepo(String path) {
        try {
            Path absolutePath = workingCopyPath("");
            String repoRoot = System.getProperty("user.dir");
            return absolutePath.relativize(Paths.get(repoRoot, path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Path workingCopyPath(String path) throws IOException {
        return Paths.get(Paths.get(RepoFiles.getPGitPath(""), "..").toString(), path);
    }

}
