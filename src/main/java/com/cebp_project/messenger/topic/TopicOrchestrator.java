package com.cebp_project.messenger.topic;

import com.cebp_project.rabbitmq.RabbitMQManager;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class TopicOrchestrator {
    private static TopicOrchestrator instance;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<TopicMessage>> topicMessages;
    private final long maxTimeout;
    private final RabbitMQManager rabbitMQManager;

    private TopicOrchestrator(long maxTimeout) throws IOException, TimeoutException {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentHashMap<>();
        this.rabbitMQManager = RabbitMQManager.getInstance();
        startGarbageCollector();
    }

    public static synchronized TopicOrchestrator getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new TopicOrchestrator(5000);
        }
        return instance;
    }

    public void publishMessage(TopicMessage message) throws IOException {
        topicMessages.computeIfAbsent(message.getType(), k -> new ConcurrentLinkedQueue<>()).add(message);
        rabbitMQManager.publishTopicMessage(message); // Publish to RabbitMQ
    }

    public List<TopicMessage> readMessages(String type) {
        ConcurrentLinkedQueue<TopicMessage> messages = topicMessages.getOrDefault(type, new ConcurrentLinkedQueue<>());
        return new ArrayList<>(messages);
    }

    public List<TopicMessage> getAllMessages() {
        List<TopicMessage> allMessages = new ArrayList<>();
        for (ConcurrentLinkedQueue<TopicMessage> queue : topicMessages.values()) {
            allMessages.addAll(queue);
        }
        return allMessages;
    }

    public int size(String type) {
        ConcurrentLinkedQueue<TopicMessage> queue = topicMessages.get(type);
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
