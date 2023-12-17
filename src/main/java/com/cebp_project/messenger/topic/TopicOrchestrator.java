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

public class TopicOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(TopicOrchestrator.class);
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<TopicMessage>> topicMessages;
    private final long maxTimeout;
    private final RabbitMQManager serverRabbitMQManager;
    private final ConcurrentHashMap<String, Set<TopicMessage>> deliveredMessages;
    private Thread garbageCollectorThread;

    public TopicOrchestrator(long maxTimeout, RabbitMQManager serverRabbitMQManager) {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentHashMap<>();
        this.serverRabbitMQManager = serverRabbitMQManager;
        this.deliveredMessages = new ConcurrentHashMap<>();
        startGarbageCollector();
    }


    public void publishMessage(TopicMessage message) throws IOException {
        logger.info("Publishing message to topic [{}]: {}", message.getType(), message.getContent());
        topicMessages.computeIfAbsent(message.getType(), k -> new ConcurrentLinkedQueue<>()).add(message);
        // Try to publish to the Server's RabbitMQ (which will be read by the ViralService)
        serverRabbitMQManager.publishTopicMessage(message);
    }

    public List<TopicMessage> readMessages(String type) {
        ConcurrentLinkedQueue<TopicMessage> messages = topicMessages.getOrDefault(type, new ConcurrentLinkedQueue<>());
        return new ArrayList<>(messages);
    }

    public int size(String type) {
        ConcurrentLinkedQueue<TopicMessage> queue = topicMessages.get(type);
        return (queue != null) ? queue.size() : 0;
    }

    public boolean hasClientReceivedMessage(String clientName, TopicMessage message) {
        return deliveredMessages.getOrDefault(clientName, Collections.emptySet()).contains(message);
    }

    public void markMessageAsDeliveredToClient(String clientName, TopicMessage message) {
        deliveredMessages.computeIfAbsent(clientName, k -> ConcurrentHashMap.newKeySet()).add(message);
    }

    private void startGarbageCollector() {
        garbageCollectorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long currentTime = System.currentTimeMillis();
                    topicMessages.forEach((type, queue) -> {
                        queue.removeIf(message -> {
                            boolean shouldRemove = currentTime - message.getSentTime() > maxTimeout;
                            if (shouldRemove) {
                                logger.info("Removing expired message from topic [{}]: {}", type, message.getContent());
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
        garbageCollectorThread.setDaemon(true);
        garbageCollectorThread.start();
    }

    public void stopGarbageCollector() {
        logger.info("Stopping Topic Orchestrator's garbage collector");
        if (garbageCollectorThread != null) {
            garbageCollectorThread.interrupt();
        }
    }
}
