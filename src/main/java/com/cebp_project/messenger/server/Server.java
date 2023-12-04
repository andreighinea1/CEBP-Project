package com.cebp_project.messenger.server;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {
    private final Map<String, Client> clients;
    private final Map<String, List<Client>> topicSubscribers; // Keep track of topic subscribers

    public Server() {
        this.clients = new ConcurrentHashMap<>();
        this.topicSubscribers = new ConcurrentHashMap<>();
    }

    public void registerClient(String name, Client client) {
        clients.put(name, client);
    }

    public void subscribeClientToTopic(String topic, Client client) {
        topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(client);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            processDirectMessages();
            processTopicMessages();
        }
    }

    private void processDirectMessages() {
        Message message = MessageQueue.getInstance().poll(); // Use poll instead of take to avoid blocking
        if (message != null) {
            String recipient = message.getRecipient();
            Client recipientClient = clients.get(recipient);
            if (recipientClient != null) {
                recipientClient.receiveMessage(message);
            }
        }
    }

    private void processTopicMessages() {
        // Handle topic messages from TopicOrchestrator
        List<TopicMessage> allTopicMessages = TopicOrchestrator.getAllMessages();
        for (TopicMessage topicMessage : allTopicMessages) {
            List<Client> subscribers = topicSubscribers.get(topicMessage.getType());
            if (subscribers != null) {
                for (Client subscriber : subscribers) {
                    subscriber.receiveTopicMessage(topicMessage);
                }
            }
        }
    }
}