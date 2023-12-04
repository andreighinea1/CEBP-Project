package com.cebp_project.messenger.server;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {
    private final Map<String, Client> clients;

    public Server() {
        this.clients = new ConcurrentHashMap<>();
    }

    public void registerClient(String name, Client client) {
        clients.put(name, client);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // TODO-ale-2: This should also handle topic messages from TopicOrchestrator (done in a similar way)

            Message message = MessageQueue.getInstance().poll(); // Use poll instead of take to avoid blocking
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
