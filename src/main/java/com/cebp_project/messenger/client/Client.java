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

/**
 * Represents a client in the messaging system.
 * This class is responsible for sending messages, subscribing to topics,
 * and handling received messages.
 */
public class Client implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final String name;
    private final MessageQueue messageQueue;
    private final Server server;
    private final List<String> otherClients;
    private final TopicOrchestrator topicOrchestrator;
    private final List<String> subscribedTopics;

    /**
     * Creates a new Client instance.
     *
     * @param name         The name of the client.
     * @param otherClients A list of other client names in the network.
     * @param server       The server to which the client is connected.
     * @param topics       A list of topics to which the client subscribes.
     */
    public Client(String name, List<String> otherClients, Server server, List<String> topics) {
        this.name = name;
        this.otherClients = otherClients;
        this.server = server;
        this.messageQueue = server.getMessageQueue();
        this.topicOrchestrator = server.getTopicOrchestrator();
        this.subscribedTopics = topics;
    }

    /**
     * The main run method of the client. Handles registration,
     * topic subscription, and message sending.
     */
    @Override
    public void run() {
        logger.info("Client [{}] started", name);
        server.registerClient(name, this);
        subscribeToTopics();

        try {
            sendMockMessages();
            for (String topic : subscribedTopics) {
                publishTopicMessages(topic);
            }
        } catch (Exception e) {
            logger.error("Error in Client [{}]: {}", name, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Subscribes the client to its designated topics.
     */
    private void subscribeToTopics() {
        for (String topic : subscribedTopics) {
            server.subscribeClientToTopic(topic, this);
        }
    }

    /**
     * Sends a set of mock messages to other clients.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping.
     * @throws IOException          if there is an issue sending messages through RabbitMQ.
     */
    private void sendMockMessages() throws InterruptedException, IOException {
        logger.info("Client [{}] sending mock messages", name);
        String[] mockMessages = {
                "Hello from " + name + " #welcome",
                "Enjoying Java programming #java #coding"
        };

        for (String messageContent : mockMessages) {
            for (String clientName : otherClients) {
                if (!clientName.equals(this.name)) {
                    publishOneBroadcastMessage(clientName, messageContent);
                }
            }
        }
    }

    /**
     * Publishes messages to a specific topic.
     *
     * @param topic The topic to which messages are published.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     * @throws IOException if there is an issue publishing messages through RabbitMQ.
     */
    private void publishTopicMessages(String topic) throws InterruptedException, IOException {
        logger.info("Client [{}] publishing topic messages to topic {}", name, topic);

        // Message that doesn't expire
        publishOneTopicMessage(topic, "TopicMsg from " + name + " #" + topic);
    }

    /**
     * Sends a mock broadcast message to a specific client.
     *
     * @param clientName The name of the client to whom the message will be sent.
     * @param messageContent The content of the message.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     * @throws IOException if there is an issue sending the message through RabbitMQ.
     */
    private void publishOneBroadcastMessage(String clientName, String messageContent) throws InterruptedException, IOException {
        try {
            // Simulate a random delay until the message would be sent (delay before to not enter the queue instantly)
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
            // Then send the msg
            messageQueue.sendMessage(new Message(this.name, clientName, messageContent));
        } catch (IllegalStateException e) {
            logger.error("Queue full, couldn't send broadcast message: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error("Error from RabbitMQ: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Publishes a single message to a given topic.
     *
     * @param topic The topic to which the message is published.
     * @param content The content of the message.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     * @throws IOException if there is an issue publishing the message through RabbitMQ.
     */
    private void publishOneTopicMessage(String topic, String content) throws InterruptedException, IOException {
        try {
            // Simulate a random delay until the message would be sent (delay before to not enter the queue instantly)
            Thread.sleep(ThreadLocalRandom.current().nextInt(250, 500));
            // Then send the msg
            topicOrchestrator.publishMessage(new TopicMessage(topic, content));
        } catch (IOException e) {
            logger.error("Error from RabbitMQ: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Handles the reception of a direct message from another client.
     *
     * @param message The message received from another client.
     */
    public void receiveMessage(Message message) {
        logger.info("{} received: {}", name, message);
    }

    /**
     * Handles the reception of a message published to a subscribed topic.
     *
     * @param topicMessage The message received from the topic.
     */
    public void receiveTopicMessage(TopicMessage topicMessage) {
        logger.info("{} received topic message: {}", name, topicMessage);
    }

    // Getters
    public String getName() {
        return name;
    }
}
