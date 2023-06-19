package com.example.assignmentdistributed;

import com.example.assignmentdistributed.server.Server;

import java.io.IOException;

public class StartServer {
    public static void main(String[] args) {
        Server server = new Server(12345);

        Thread thread = new Thread(()-> {
            try {
                server.start();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }
}