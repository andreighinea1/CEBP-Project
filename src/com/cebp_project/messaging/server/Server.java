package com.cebp_project.messaging.server;

import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.topic.Topic;

public class Server implements Runnable {
    private MessageQueue messageQueue;
    private Topic topic;

    public Server(MessageQueue messageQueue, Topic topic) {
        this.messageQueue = messageQueue;
        this.topic = topic;
    }

    @Override
    public void run() {
        while (true) {
            // The server can perform additional tasks here
        }
    }
}
