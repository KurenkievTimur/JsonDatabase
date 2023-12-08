package com.kurenkievtimur.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public Session(Socket socket) throws IOException {
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void run() {
        try {
            Server.handleRequest(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}