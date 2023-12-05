package com.cebp_project.messenger.topic;

import com.cebp_project.rabbitmq.RabbitMQManager;
import com.cebp_project.messenger.topic.TopicMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class TopicOrchestrator {
    private static TopicOrchestrator instance;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<TopicMessage>> topicMessages;
    private final long maxTimeout;
    private final RabbitMQManager rabbitMQManager;

    private TopicOrchestrator(long maxTimeout) {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentHashMap<>();
        try {
            this.rabbitMQManager = RabbitMQManager.getInstance();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Unable to initialize RabbitMQManager", e);
        }
        startGarbageCollector();
    }

    public static synchronized TopicOrchestrator getInstance() {
        if (instance == null) {
            instance = new TopicOrchestrator(5000); // Use the default timeout value you want for garbage collection
        }
        return instance;
    }

    public static void publishMessage(TopicMessage message) throws IOException {
        TopicOrchestrator orchestrator = getInstance();
        orchestrator.topicMessages.computeIfAbsent(message.getType(), k -> new ConcurrentLinkedQueue<>()).add(message);
        orchestrator.rabbitMQManager.publishTopicMessage(message); // Publish to RabbitMQ
    }

    public static List<TopicMessage> readMessages(String type) {
        TopicOrchestrator orchestrator = getInstance();
        return new ArrayList<>(orchestrator.topicMessages.getOrDefault(type, new ConcurrentLinkedQueue<>()));
    }

    public static List<TopicMessage> getAllMessages() {
        TopicOrchestrator orchestrator = getInstance();
        List<TopicMessage> allMessages = new ArrayList<>();
        orchestrator.topicMessages.values().forEach(allMessages::addAll);
        return allMessages;
    }

    public static int size(String type) {
        TopicOrchestrator orchestrator = getInstance();
        ConcurrentLinkedQueue<TopicMessage> queue = orchestrator.topicMessages.get(type);
        return (queue != null) ? queue.size() : 0;
    }

    private void startGarbageCollector() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    long currentTime = System.currentTimeMillis();
                    topicMessages.forEach((type, queue) -> {
                        queue.removeIf(message -> currentTime - message.getSentTime() > maxTimeout);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
