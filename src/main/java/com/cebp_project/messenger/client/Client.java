package com.cebp_project.messenger.client;

import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.server.Server;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Client implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final String name;
    private final MessageQueue messageQueue;
    private final Server server;
    private final List<String> otherClients;
    private final TopicOrchestrator topicOrchestrator;

    private final String topicToUse = "commonTopic";

    public Client(String name, List<String> otherClients, Server server) {
        this.name = name;
        this.otherClients = otherClients;
        this.server = server;
        this.messageQueue = server.getMessageQueue();
        this.topicOrchestrator = server.getTopicOrchestrator();
    }


    public void receiveMessage(Message message) {
        logger.info("{} received: {}", name, message);
    }

    public void receiveTopicMessage(TopicMessage topicMessage) {
        logger.info("{} received topic message: {}", name, topicMessage);
    }

    @Override
    public void run() {
        logger.info("Client [{}] started", name);
        server.registerClient(name, this); // Register client with the server
        server.subscribeClientToTopic(topicToUse, this); // Register client with the server
        try {
            sendMockMessages();
            publishTopicMessages();
        } catch (InterruptedException | IllegalStateException e) {
            logger.error("Error in Client [{}]: {}", name, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void sendMockMessages() throws InterruptedException {
        logger.debug("Client [{}] sending mock messages", name);
        String[] mockMessages = {
                "Hello from " + name + " #welcome",
                "Enjoying Java programming #java #coding"
        };

        for (String messageContent : mockMessages) {
            for (String clientName : otherClients) {
                if (!clientName.equals(this.name)) {
                    try {
                        messageQueue.sendMessage(new Message(this.name, clientName, messageContent, System.currentTimeMillis()));
                    } catch (IllegalStateException e) {
                        logger.error("Queue full, couldn't send broadcast msg");
                    }
                }
            }
            // Simulate a random delay for sending messages
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 250));
        }
    }

    private void publishTopicMessages() throws InterruptedException {
        // Publish topic messages
        logger.debug("Client [{}] publishing topic messages", name);
        topicOrchestrator.publishMessage(new TopicMessage(topicToUse, "EXPIRED Broadcast from " + name));
//        Thread.sleep(ThreadLocalRandom.current().nextInt(0, 3500));  // The msg won't expire
        Thread.sleep(5500 + ThreadLocalRandom.current().nextInt(0, 1000));  // The msg will expire

        // Publish a message to the topic
        topicOrchestrator.publishMessage(new TopicMessage(topicToUse, "Broadcast from " + name + " #topic"));
        // Simulate a random delay for listening to topic
        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 1500));
    }
}
