package com.cebp_project.messenger.client;

import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.server.Server;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Client implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final String name;
    private final MessageQueue messageQueue;
    private final Server server;
    private final List<String> otherClients;

    public Client(String name, MessageQueue messageQueue, List<String> otherClients, Server server) {
        this.name = name;
        this.messageQueue = messageQueue;
        this.otherClients = otherClients;
        this.server = server;
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
        try {
            sendMockMessages();
            receiveMessages();
            publishAndListenToTopicMessages();
        } catch (InterruptedException | IllegalStateException | IOException e) {
            logger.error("Error in Client [{}]: {}", name, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void sendMockMessages() throws InterruptedException, IOException {
        logger.debug("Client [{}] sending mock messages", name);
        String[] mockMessages = {
                "Hello from " + name + " #welcome",
                "Enjoying Java programming #java #coding"
        };

        for (String messageContent : mockMessages) {
            for (String clientName : otherClients) {
                if (!clientName.equals(this.name)) {
                    messageQueue.sendMessage(new Message(this.name, clientName, messageContent, System.currentTimeMillis()));
                }
            }
            // Simulate a random delay for sending messages
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 250));
        }
    }

    private void receiveMessages() {
        logger.debug("Client [{}] receiving messages", name);
        for (int i = 0; i < otherClients.size() - 1; i++) {
            Message receivedMessage = messageQueue.receiveMessage(name);
            if (receivedMessage != null) {
                logger.info("{} received from {}: {}", name, receivedMessage.getSender(), receivedMessage.getContent());
            }
        }
    }

    private void publishAndListenToTopicMessages() throws InterruptedException, IOException {
        logger.debug("Client [{}] publishing and listening to topic messages", name);
        TopicOrchestrator.getInstance().publishMessage(new TopicMessage("commonTopic", "FAST Broadcast from " + name));
        Thread.sleep(4500 + ThreadLocalRandom.current().nextInt(0, 1000));

        TopicOrchestrator.getInstance().publishMessage(new TopicMessage("commonTopic", "Broadcast from " + name));
        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 1500));

        List<TopicMessage> topicMessages = TopicOrchestrator.getInstance().readMessages("commonTopic");
        logger.info("{} reads from topic: {}", name, topicMessages);
    }
}
