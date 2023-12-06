package com.cebp_project.messenger.server;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class Server implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Map<String, Client> clients;
    private final Map<String, List<Client>> topicSubscribers;

    public Server() {
        this.clients = new ConcurrentHashMap<>();
        this.topicSubscribers = new ConcurrentHashMap<>();
    }

    public void registerClient(String name, Client client) {
        clients.put(name, client);
        logger.info("Client registered: {}", name);
    }

    public void subscribeClientToTopic(String topic, Client client) {
        topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(client);
        logger.info("Client subscribed to topic: {}", topic);
    }

    @Override
    public void run() {
        logger.info("Server started");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processDirectMessages();
                processTopicMessages();
            } catch (Exception e) {
                logger.error("Error in Server run loop", e);
            }
        }
    }

    private void processDirectMessages() throws IOException, TimeoutException {
        Message message = MessageQueue.getInstance().poll();
        if (message != null) {
            Client recipientClient = clients.get(message.getRecipient());
            if (recipientClient != null) {
                recipientClient.receiveMessage(message);
            }
        }
    }

    private void processTopicMessages() {
        List<TopicMessage> allTopicMessages = TopicOrchestrator.getInstance().getAllMessages();
        for (TopicMessage topicMessage : allTopicMessages) {
            List<Client> subscribers = topicSubscribers.get(topicMessage.getType());
            if (subscribers != null) {
                for (Client subscriber : subscribers) {
                    subscriber.receiveTopicMessage(topicMessage);
                }
            }
        }
        TopicOrchestrator.getInstance().clearMessages();
    }
}
