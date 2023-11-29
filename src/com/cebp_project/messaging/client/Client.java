package com.cebp_project.messaging.client;

import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.topic.TopicMessage;
import com.cebp_project.messaging.topic.TopicOrchestrator;

import java.util.List;

public class Client implements Runnable {
    private final String name;
    private final MessageQueue messageQueue;
    private final List<String> otherClients;

    public Client(String name, MessageQueue messageQueue, List<String> otherClients) {
        this.name = name;
        this.messageQueue = messageQueue;
        this.otherClients = otherClients;
    }

    @Override
    public void run() {
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

            // Publish a message to the topic
            TopicOrchestrator.publishMessage(new TopicMessage("commonTopic", "Broadcast from " + name, 1000));

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
