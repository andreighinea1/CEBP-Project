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
            try {
                // Process messages from the main message queue
                processMessages();  // TODO: Also add for the Topics?

                // Additional tasks like client health checks, logging, etc.
                performHealthChecks();
                performLogging();

                // Simulate a delay
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void processMessages() {
        // Implement message routing logic
        // Here's where the Kafka/RabbitMQ routing would go
    }

    private void performHealthChecks() {
        // TODO: Implement server health checks
        //  Could include checking client connectivity, server resource usage, etc.
    }

    private void performLogging() {
        // TODO
    }
}

