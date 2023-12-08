package com.kurenkievtimur.server;

import com.google.gson.*;
import com.kurenkievtimur.common.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    private static ServerSocket server;
    private static ExecutorService executor;

    public static void main(String[] args) throws IOException {
        startServer();
    }

    private static void startServer() throws IOException {
        System.out.println("Server started!");

        server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS));
        executor = Executors.newFixedThreadPool(8);

        while (!server.isClosed()) {
            executor.submit(new Session(server.accept()));
        }
    }

    public static void handleRequest(Session session) throws IOException {
        try (
                DataInputStream input = session.getInputStream();
                DataOutputStream output = session.getOutputStream()
        ) {
            JsonObject message = new Gson().fromJson(input.readUTF(), JsonObject.class);
            String type = message.get("type").getAsString();
            switch (type) {
                case "get" -> handleGetRequest(output, message);
                case "set" -> handleSetRequest(output, message);
                case "delete" -> handleDeleteRequest(output, message);
                case "exit" -> handleExitRequest(output);
            }
        }
    }

    public static void handleGetRequest(DataOutputStream output, JsonObject message) throws IOException {
        String response = getRequest(message);
        output.writeUTF(response);
    }

    public static void handleSetRequest(DataOutputStream output, JsonObject message) throws IOException {
        String response = setRequest(message);
        output.writeUTF(response);
    }

    public static void handleDeleteRequest(DataOutputStream output, JsonObject message) throws IOException {
        String response = deleteRequest(message);
        output.writeUTF(response);
    }

    public static void handleExitRequest(DataOutputStream output) throws IOException {
        String response = exitRequest();
        output.writeUTF(response);

        executor.shutdown();
        server.close();
    }

    private static String getRequest(JsonObject message) throws IOException {
        JsonObject jsonObject = Utils.loadJsonObject();

        JsonElement messageKey = message.get("key");
        if (messageKey.isJsonArray()) {
            return getNestedValue(jsonObject, message);
        }

        Gson gson = new Gson();

        String key = messageKey.getAsString();
        JsonElement jsonElement = jsonObject.get(key);

        if (jsonElement == null) {
            JsonObject response = new JsonObject();
            response.add("response", new JsonPrimitive("ERROR"));
            response.add("reason", new JsonPrimitive("No such key"));

            return gson.toJson(response);
        }

        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));
        response.add("value", jsonElement);

        return gson.toJson(response);
    }

    private static String getNestedValue(JsonObject jsonObject, JsonObject message) {
        JsonObject currentObject = jsonObject;
        Gson gson = new Gson();

        JsonArray jsonArray = message.get("key").getAsJsonArray();
        for (int i = 0; i < jsonArray.size() - 1; i++) {
            String key = jsonArray.get(i).getAsString();

            JsonObject nestedObject = currentObject.getAsJsonObject(key);
            if (nestedObject == null) {
                JsonObject response = new JsonObject();
                response.add("response", new JsonPrimitive("ERROR"));
                response.add("reason", new JsonPrimitive("No such key"));

                return gson.toJson(response);
            }

            currentObject = nestedObject;
        }

        String lastKey = jsonArray.get(jsonArray.size() - 1).getAsString();

        JsonElement jsonElement = currentObject.get(lastKey);
        if (jsonElement == null) {
            JsonObject response = new JsonObject();
            response.add("response", new JsonPrimitive("ERROR"));
            response.add("reason", new JsonPrimitive("No such key"));

            return gson.toJson(response);
        }

        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));
        response.add("value", jsonElement);

        return gson.toJson(response);
    }

    private static String setRequest(JsonObject message) throws IOException {
        JsonObject jsonObject = Utils.loadJsonObject();
        Gson gson = new Gson();

        JsonElement messageKey = message.get("key");
        if (messageKey.isJsonArray()) {
            return addNestedValue(jsonObject, message);
        }

        String key = messageKey.getAsString();
        jsonObject.add(key, message.get("value"));

        Utils.writeJsonServerFile(gson.toJson(jsonObject).getBytes());

        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));

        return gson.toJson(response);
    }

    private static String addNestedValue(JsonObject jsonObject, JsonObject message) throws IOException {
        JsonObject currentObject = jsonObject;

        JsonArray jsonArray = message.get("key").getAsJsonArray();
        for (int i = 0; i < jsonArray.size() - 1; i++) {
            String key = jsonArray.get(i).getAsString();

            JsonObject nestedObject = currentObject.getAsJsonObject(key);
            if (nestedObject == null) {
                nestedObject = new JsonObject();
                currentObject.add(key, nestedObject);
            }

            currentObject = nestedObject;
        }

        String lastKey = jsonArray.get(jsonArray.size() - 1).getAsString();
        currentObject.add(lastKey, message.get("value"));

        Utils.writeJsonServerFile(new Gson().toJson(jsonObject).getBytes());

        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));

        return new Gson().toJson(response);
    }

    private static String deleteRequest(JsonObject message) throws IOException {
        JsonObject jsonObject = Utils.loadJsonObject();
        Gson gson = new Gson();

        JsonElement messageKey = message.get("key");
        if (messageKey.isJsonArray()) {
            return deleteNestedKey(jsonObject, message);
        }

        String lastKey = message.get("key").getAsString();
        JsonElement remove = jsonObject.remove(lastKey);

        if (remove == null) {
            JsonObject response = new JsonObject();
            response.add("response", new JsonPrimitive("ERROR"));
            response.add("reason", new JsonPrimitive("No such key"));

            return new Gson().toJson(response);
        }

        Utils.writeJsonServerFile(gson.toJson(jsonObject).getBytes());

        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));

        return new Gson().toJson(response);
    }

    private static String deleteNestedKey(JsonObject jsonObject, JsonObject message) throws IOException {
        JsonObject currentObject = jsonObject;

        JsonArray jsonArray = message.get("key").getAsJsonArray();
        for (int i = 0; i < jsonArray.size() - 1; i++) {
            String key = jsonArray.get(i).getAsString();

            JsonObject nestedObject = currentObject.getAsJsonObject(key);
            if (nestedObject == null) {
                JsonObject response = new JsonObject();
                response.add("response", new JsonPrimitive("ERROR"));
                response.add("reason", new JsonPrimitive("No such key"));

                return new Gson().toJson(response);
            }

            currentObject = nestedObject;
        }

        String lastKey = jsonArray.get(jsonArray.size() - 1).getAsString();

        JsonElement remove = currentObject.remove(lastKey);
        if (remove == null) {
            JsonObject response = new JsonObject();
            response.add("response", new JsonPrimitive("ERROR"));
            response.add("reason", new JsonPrimitive("No such key"));

            return new Gson().toJson(response);
        }

        Utils.writeJsonServerFile(new Gson().toJson(jsonObject).getBytes());

        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));

        return new Gson().toJson(response);
    }

    private static String exitRequest() {
        JsonObject response = new JsonObject();
        response.add("response", new JsonPrimitive("OK"));

        return new Gson().toJson(response);
    }
}