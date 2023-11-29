package com.cebp_project.messaging.server;

import com.cebp_project.messaging.client.Client;
import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.message.MessageQueue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {
    private final MessageQueue messageQueue;
    private final Map<String, Client> clients;

    public Server(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
        this.clients = new ConcurrentHashMap<>();
    }

    public void registerClient(String name, Client client) {
        clients.put(name, client);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Message message = messageQueue.poll(); // Use poll instead of take to avoid blocking
            if (message != null) {
                String recipient = message.getRecipient();
                Client recipientClient = clients.get(recipient);
                if (recipientClient != null) {
                    recipientClient.receiveMessage(message);
                }
            }
        }
    }
}
