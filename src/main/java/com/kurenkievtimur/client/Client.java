package com.kurenkievtimur.client;

import com.kurenkievtimur.client.argument.Args;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kurenkievtimur.common.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static void main(String[] args) throws IOException {
        startClient(args);
    }

    public static void startClient(String[] args) throws IOException {
        System.out.println("Client started!");
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            Args arguments = Args.parseArguments(args);
            JsonObject jsonObject = convertArgumentsToJsonObject(arguments);

            sendRequest(input, output, jsonObject);
        }
    }

    public static void sendRequest(DataInputStream input, DataOutputStream output, JsonObject jsonObject) throws IOException {
        String messageGson = new Gson().toJson(jsonObject);

        output.writeUTF(messageGson);
        System.out.printf("Sent: %s\n", messageGson);

        String response = input.readUTF();
        System.out.printf("Received: %s\n", response);
    }

    private static JsonObject convertArgumentsToJsonObject(Args args) throws IOException {
        if (args.getInput() != null) {
            String json = Utils.readJsonClientFile(args.getInput());
            return new Gson().fromJson(json, JsonObject.class);
        }

        Gson gson = new Gson();
        String json = gson.toJson(args);

        return gson.fromJson(json, JsonObject.class);
    }
}