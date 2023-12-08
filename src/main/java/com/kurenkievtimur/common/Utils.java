package com.kurenkievtimur.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Utils {
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final String DB_FILE = "db.json";

    public static String readJsonClientFile(String fileName) throws IOException {
        Path path = Paths.get(getClientFilePath(fileName));
        byte[] bytes = Files.readAllBytes(path);

        return new String(bytes);
    }

    public static JsonObject loadJsonObject() throws IOException {
        Lock lock = LOCK.readLock();

        String serverFile;
        try {
            lock.lock();
            serverFile = readJsonServerFile();
        } finally {
            lock.unlock();
        }

        if (serverFile.isEmpty()) {
            return new JsonObject();
        }

        return JsonParser.parseString(serverFile).getAsJsonObject();
    }

    private static String readJsonServerFile() throws IOException {
        Path path = Paths.get(getServerFilePath());

        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }

    public static void writeJsonServerFile(byte[] bytes) throws IOException {
        Lock lock = LOCK.writeLock();
        Path path = Paths.get(getServerFilePath());

        try {
            lock.lock();
            Files.write(path, bytes);
        } finally {
            lock.unlock();
        }
    }

    private static String getClientFilePath(String name) {
        return System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" +
                File.separator + "resources" + File.separator + name;
    }

    private static String getServerFilePath() {
        return System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" +
                File.separator + "resources" + File.separator + DB_FILE;
    }
}
