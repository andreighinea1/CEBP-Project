package com.cebp_project.messenger.client;

import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.server.Server;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Client implements Runnable {
    // TODO-all-last: You may all need to modify this class, leave it for the last,
    //  and tell ChatGPT to modify the examples accordingly after your changes in other classes
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
        System.out.println(name + " received: " + message);
    }

    public void receiveTopicMessage(TopicMessage topicMessage) {
        System.out.println(name + " received topic message: " + topicMessage);
    }

    @Override
    public void run() {
        server.registerClient(name, this); // Register client with the server
        try {
            sendMockMessages();
            receiveMessages();
            publishAndListenToTopicMessages();
        } catch (InterruptedException | IllegalStateException | IOException e) {
            System.out.println("Error in Client [" + name + "]: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void sendMockMessages() throws InterruptedException, IOException {
        // Sending mock messages with hashtags to other clients
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
        for (int i = 0; i < otherClients.size() - 1; i++) {
            Message receivedMessage = messageQueue.receiveMessage(name);
            if (receivedMessage != null) {
                System.out.println(name + " received from " + receivedMessage.getSender() + ": " + receivedMessage.getContent());
            }
        }
    }

    private void publishAndListenToTopicMessages() throws InterruptedException, IOException {
        // Publish topic messages
        TopicOrchestrator.getInstance().publishMessage(new TopicMessage("commonTopic", "FAST Broadcast from " + name));
//        Thread.sleep(ThreadLocalRandom.current().nextInt(0, 3500));  // The msg won't expire
        Thread.sleep(4500 + ThreadLocalRandom.current().nextInt(0, 1000));  // The msg will expire

        // Publish a message to the topic
        TopicOrchestrator.getInstance().publishMessage(new TopicMessage("commonTopic", "Broadcast from " + name));
        // Simulate a random delay for listening to topic
        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 1500));

        // Listening to the topic
        List<TopicMessage> topicMessages = TopicOrchestrator.getInstance().readMessages("commonTopic");
        System.out.println(name + " reads from topic: " + topicMessages);
    }
}
