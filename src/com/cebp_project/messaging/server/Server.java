package com.cebp_project.messaging.server;

import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.client.Client;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {
    private final MessageQueue messageQueue;
    private final Map<String, Client> clients;

    public Server(MessageQueue messageQueue, List<String> clientNames) {
        this.messageQueue = messageQueue;
        this.clients = new ConcurrentHashMap<>();
        // Removed the initial population with null values
    }

    public void registerClient(String name, Client client) {
        clients.put(name, client); // Clients are added here when they register
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = messageQueue.poll(); // Use poll instead of take to avoid blocking
                if (message != null) {
                    String recipient = message.getRecipient();
                    Client recipientClient = clients.get(recipient);
                    if (recipientClient != null) {
                        recipientClient.receiveMessage(message);
                    }
                }
                // Simulate a delay
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
