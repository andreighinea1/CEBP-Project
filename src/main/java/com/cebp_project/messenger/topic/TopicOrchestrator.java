package com.cebp_project.messenger.topic;

import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Orchestrates topic-based messaging within the system, handling message storage, delivery tracking, and garbage collection.
 */
public class TopicOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(TopicOrchestrator.class);
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<TopicMessage>> topicMessages;
    private final long maxTimeout;
    private final RabbitMQManager serverRabbitMQManager;
    private final ConcurrentHashMap<String, Set<TopicMessage>> deliveredMessages;
    private Thread garbageCollectorThread;

    /**
     * Initializes a new instance of the TopicOrchestrator.
     *
     * @param maxTimeout            The maximum time in milliseconds a message remains valid.
     * @param serverRabbitMQManager The RabbitMQ manager for handling message publishing.
     */
    public TopicOrchestrator(long maxTimeout, RabbitMQManager serverRabbitMQManager) {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentHashMap<>();
        this.serverRabbitMQManager = serverRabbitMQManager;
        this.deliveredMessages = new ConcurrentHashMap<>();
        startGarbageCollector();
    }

    /**
     * Publishes a topic message both locally and to RabbitMQ.
     *
     * @param message The topic message to be published.
     * @throws IOException if there is an issue communicating with RabbitMQ.
     */
    public void publishMessage(TopicMessage message) throws IOException {
        logger.info("Publishing message to topic [{}]: {}", message.getType(), message.getContent());
        topicMessages.computeIfAbsent(message.getType(), k -> new ConcurrentLinkedQueue<>()).add(message);
        serverRabbitMQManager.publishTopicMessage(message);
    }

    /**
     * Reads all messages of a specific topic type.
     *
     * @param type The topic type.
     * @return A list of messages for the specified topic.
     */
    public List<TopicMessage> readMessages(String type) {
        ConcurrentLinkedQueue<TopicMessage> messages = topicMessages.getOrDefault(type, new ConcurrentLinkedQueue<>());
        return new ArrayList<>(messages);
    }

    /**
     * Checks if a client has already received a specific message.
     *
     * @param clientName The name of the client.
     * @param message    The message in question.
     * @return True if the client has already received the message, false otherwise.
     */
    public boolean hasClientReceivedMessage(String clientName, TopicMessage message) {
        return deliveredMessages.getOrDefault(clientName, Collections.emptySet()).contains(message);
    }

    /**
     * Marks a message as delivered to a specific client.
     *
     * @param clientName The name of the client.
     * @param message    The message that was delivered.
     */
    public void markMessageAsDeliveredToClient(String clientName, TopicMessage message) {
        deliveredMessages.computeIfAbsent(clientName, k -> ConcurrentHashMap.newKeySet()).add(message);
    }

    /**
     * Cleans a specific topic message (which expired) from all the clients.
     *
     * @param message The message to clean.
     */
    private void cleanDeliveredMessagesForMessage(TopicMessage message) {
        deliveredMessages.forEach((clientName, messages) -> messages.remove(message));
    }

    /**
     * Starts a background thread for garbage collection of messages and tracking data.
     * This thread periodically checks and removes expired messages from the topicMessages.
     * It also ensures that the tracking data in deliveredMessages is kept in sync.
     */
    private void startGarbageCollector() {
        garbageCollectorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long currentTime = System.currentTimeMillis();

                    // Iterate over each topic queue to remove expired messages.
                    topicMessages.forEach((type, queue) -> {
                        queue.removeIf(message -> {
                            boolean shouldRemove = currentTime - message.getSentTime() > maxTimeout;
                            if (shouldRemove) {
                                // Log the removal of an expired message.
                                logger.info("Removing expired message from topic [{}]: {}", type, message.getContent());

                                // Clean corresponding entries in deliveredMessages to avoid memory leaks and ensure data consistency.
                                cleanDeliveredMessagesForMessage(message);
                            }
                            return shouldRemove;
                        });
                    });

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.info("Garbage collector thread interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        garbageCollectorThread.setName("TopicOrchestrator-GarbageCollector");
        garbageCollectorThread.setDaemon(true); // Run as a daemon thread, so it does not prevent JVM shutdown.
        garbageCollectorThread.start();
    }

    /**
     * Stops the garbage collector thread.
     */
    public void stopGarbageCollector() {
        logger.info("Stopping Topic Orchestrator's garbage collector");
        if (garbageCollectorThread != null) {
            garbageCollectorThread.interrupt();
        }
    }
}
