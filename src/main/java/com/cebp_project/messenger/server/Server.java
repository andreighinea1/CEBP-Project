package com.cebp_project.messenger.server;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;
import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static com.cebp_project.messenger.constants.Constants.MessageQueueMaxSize;
import static com.cebp_project.messenger.constants.Constants.TopicOrchestratorMaxTimeout;

/**
 * Represents the server that manages message processing and distribution.
 * It handles direct client-to-client messages and messages published to topics.
 */
public class Server implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Map<String, Client> clients;
    private final Map<String, List<Client>> topicSubscribers;
    private final TopicOrchestrator topicOrchestrator;
    private final MessageQueue messageQueue;
    private final RabbitMQManager serverRabbitMQManager;
    private volatile boolean running = true;

    /**
     * Constructs a new Server instance.
     */
    public Server() {
        this.clients = new ConcurrentHashMap<>();
        this.topicSubscribers = new ConcurrentHashMap<>();
        this.serverRabbitMQManager = new RabbitMQManager();
        this.topicOrchestrator = new TopicOrchestrator(TopicOrchestratorMaxTimeout, this.serverRabbitMQManager);
        this.messageQueue = new MessageQueue(MessageQueueMaxSize, this.serverRabbitMQManager);
    }

    /**
     * Registers a client with the server.
     *
     * @param name   The name of the client.
     * @param client The client object to register.
     */
    public void registerClient(String name, Client client) {
        clients.put(name, client);
        logger.info("Client registered: {}", name);
    }

    /**
     * Subscribes a client to a specific topic.
     *
     * @param topic  The topic to subscribe to.
     * @param client The client subscribing to the topic.
     */
    public void subscribeClientToTopic(String topic, Client client) {
        topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(client);
        logger.info("Client [{}] subscribed to topic: {}", client.getName(), topic);
    }

    /**
     * The main run loop of the server.
     * Handles processing of direct messages and topic messages.
     */
    @Override
    public void run() {
        logger.info("Server started processing messages");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                processDirectMessages();
                processTopicMessages();
            } catch (Exception e) {
                logger.error("Error in Server run loop", e);
                // TODO: Determine the next steps based on the severity of the error.
            }
        }
    }

    /**
     * Processes direct messages from the message queue and delivers them to the intended recipients.
     */
    private void processDirectMessages() {
        Message message = messageQueue.poll();
        if (message != null) {
            Client recipientClient = clients.get(message.getRecipient());
            if (recipientClient != null) {
                recipientClient.receiveMessage(message);
            }
        }
    }

    /**
     * Processes messages for each topic by delivering them to subscribed clients.
     */
    private void processTopicMessages() {
        for (String topic : topicSubscribers.keySet()) {
            List<TopicMessage> messagesForTopic = topicOrchestrator.readMessages(topic);
            if (messagesForTopic != null) {
                processMessagesForTopic(topic, messagesForTopic);
            }
        }
    }

    /**
     * Delivers topic messages to all subscribed clients for a specific topic.
     *
     * @param topic    The topic for which messages are being processed.
     * @param messages The list of messages to be delivered.
     */
    private void processMessagesForTopic(String topic, List<TopicMessage> messages) {
        List<Client> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            for (TopicMessage message : messages) {
                for (Client subscriber : subscribers) {
                    if (!topicOrchestrator.hasClientReceivedMessage(subscriber.getName(), message)) {
                        subscriber.receiveTopicMessage(message);
                        topicOrchestrator.markMessageAsDeliveredToClient(subscriber.getName(), message);
                    }
                }
            }
        }
    }

    /**
     * Gracefully stops the server, closing all resources and halting message processing.
     */
    public void stopServer() {
        running = false;
        try {
            //noinspection ConstantValue
            if (serverRabbitMQManager != null) {
                serverRabbitMQManager.close();
            }
        } catch (IOException | TimeoutException e) {
            logger.error("Error closing RabbitMQManager", e);
        }
        topicOrchestrator.stopGarbageCollector();
        logger.info("Server stopped");
    }

    // Getters
    public TopicOrchestrator getTopicOrchestrator() {
        return topicOrchestrator;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }
}
