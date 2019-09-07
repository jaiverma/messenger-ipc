package com.example.qace;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Data {
    ServerSocket server;
    Socket socket;

    public Data() {
        try {
            server = new ServerSocket(6666);
        } catch (IOException e) {
        }
    }

    public void receiveFromSocket() {
        try {
            socket = server.accept();
            OutputStream output = socket.getOutputStream();
        } catch (IOException e) {
        }
    }
}
