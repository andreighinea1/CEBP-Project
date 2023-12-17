package com.cebp_project.messenger.server;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;
import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cebp_project.messenger.constants.Constants.MessageQueueMaxSize;
import static com.cebp_project.messenger.constants.Constants.TopicOrchestratorMaxTimeout;

public class Server implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Map<String, Client> clients;
    private final Map<String, List<Client>> topicSubscribers;
    private final TopicOrchestrator topicOrchestrator;
    private final MessageQueue messageQueue;
    private final RabbitMQManager serverRabbitMQManager;

    public Server() {
        this.clients = new ConcurrentHashMap<>();
        this.topicSubscribers = new ConcurrentHashMap<>();
        this.serverRabbitMQManager = new RabbitMQManager();
        this.topicOrchestrator = new TopicOrchestrator(TopicOrchestratorMaxTimeout, serverRabbitMQManager);
        this.messageQueue = new MessageQueue(MessageQueueMaxSize, serverRabbitMQManager);
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

    private void processDirectMessages() {
        Message message = messageQueue.poll();
        if (message != null) {
            Client recipientClient = clients.get(message.getRecipient());
            if (recipientClient != null) {
                recipientClient.receiveMessage(message);
            }
        }
    }

    private void processTopicMessages() {
        for (String topic : topicSubscribers.keySet()) {
            List<TopicMessage> messagesForTopic = topicOrchestrator.readMessages(topic);
            if (messagesForTopic != null) {
                processMessagesForTopic(topic, messagesForTopic);
            }
            topicOrchestrator.clearMessagesForTopic(topic);
        }
    }

    private void processMessagesForTopic(String topic, List<TopicMessage> messages) {
        List<Client> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            for (TopicMessage message : messages) {
                for (Client subscriber : subscribers) {
                    subscriber.receiveTopicMessage(message);
                }
            }
        }
    }

    // Getters
    public TopicOrchestrator getTopicOrchestrator() {
        return topicOrchestrator;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }
}
