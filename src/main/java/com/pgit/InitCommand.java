package com.pgit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

import com.github.underscore.lodash.U;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import picocli.CommandLine;
import utils.RepoFiles;
import utils.SystemConfig;

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
        if (RepoFiles.inGitiRepo()) {
            System.out.println("Already in a repo");
            return;
        }
        JsonObject repoStructure = RepoFiles.initRepoStructure(bareRepoOption);
        JsonObject metaData = RepoFiles.initRepoMetaData();
        writeFilesFromTree(bareRepoOption ? repoStructure.getAsJsonObject(".giti") : repoStructure,
                System.getProperty("user.dir"), metaData);
        System.out.println("Initialized empty Giti repository");
    }

    void writeFilesFromTree(JsonObject structure, String prefix, JsonObject metadata) {
        for (Map.Entry<String, JsonElement> entry: structure.entrySet()) {
            Path path = Paths.get(prefix, entry.getKey());
            if (metadata.get(entry.getKey()) != null) {
                if (metadata.get(entry.getKey()).getAsString().equalsIgnoreCase("file")) {
                    try {
                        File newCreatedFile = new File(path.toString());
                        boolean fileIsCreated = newCreatedFile.createNewFile();
                        if (fileIsCreated) {
                            FileWriter fileWriter = new FileWriter(newCreatedFile);
                            if(entry.getValue().isJsonPrimitive()){
                                String value =  entry.getValue().getAsString();
                                fileWriter.write(value);
                            }else{
                                fileWriter.write(U.formatJson(entry.getValue().toString()));
                            }
                            fileWriter.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (SystemConfig.isPosix) {
                            Files.createDirectory(path,
                                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
                        } else {
                            Files.createDirectory(path);
                        }
                        if (entry.getValue().isJsonObject()){
                            writeFilesFromTree(entry.getValue().getAsJsonObject(), path.toString(), metadata);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class PGitStructure {
        String Head;
        Config config;

        public static class Config {
            HashMap<String, Object> core;

            public Config(HashMap<String, Object> core) {
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

}
