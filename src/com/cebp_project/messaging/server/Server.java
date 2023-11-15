package com.cebp_project.messaging.server;

import com.cebp_project.messaging.message.MessageQueue;

public class Server implements Runnable {
    private final MessageQueue messageQueue;

    public Server(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            // The server can perform additional tasks here
        }
    }
}
