package com.cebp_project.messaging.client;

import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.server.Server;
import com.cebp_project.messaging.topic.TopicMessage;
import com.cebp_project.messaging.topic.TopicOrchestrator;

import java.util.List;

public class Client implements Runnable {
    // TODO: Make a main method to start all clients in separate processes
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
        // Handle received message
        System.out.println(name + " received: " + message);
    }

    @Override
    public void run() {
        server.registerClient(name, this); // Register client with the server

        try {
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
            }

            // Simulate a delay for sending messages
            Thread.sleep(1000);

            // Receiving messages
            for (int i = 0; i < otherClients.size() - 1; i++) {
                Message receivedMessage = messageQueue.receiveMessage(name);
                if (receivedMessage != null) {
                    System.out.println(name + " received from " + receivedMessage.getSender() + ": " + receivedMessage.getContent());
                }
            }

            // Publish topic messages
            TopicOrchestrator.publishMessage(new TopicMessage("commonTopic", "FAST Broadcast from " + name));
//            Thread.sleep(3500);  // With this the msg won't expire
            Thread.sleep(4500);  // With this the msg will expire

            // Publish a message to the topic
            TopicOrchestrator.publishMessage(new TopicMessage("commonTopic", "Broadcast from " + name));

            // Simulate a delay for listening to topic
            Thread.sleep(1000);

            // Listening to the topic
            List<TopicMessage> topicMessages = TopicOrchestrator.readMessages("commonTopic");
            System.out.println(name + " reads from topic: " + topicMessages);
        } catch (InterruptedException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
