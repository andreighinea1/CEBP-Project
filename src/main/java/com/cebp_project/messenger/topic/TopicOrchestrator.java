package com.cebp_project.messenger.topic;

import com.cebp_project.rabbitmq.RabbitMQManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TopicOrchestrator {
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<TopicMessage>> topicMessages;
    private final long maxTimeout;
    private final RabbitMQManager serverRabbitMQManager;
    private Thread garbageCollectorThread;


    public TopicOrchestrator(long maxTimeout, RabbitMQManager serverRabbitMQManager) {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentHashMap<>();
        this.serverRabbitMQManager = serverRabbitMQManager;
        startGarbageCollector();
    }

    public void publishMessage(TopicMessage message) throws IOException {
        topicMessages.computeIfAbsent(message.getType(), k -> new ConcurrentLinkedQueue<>()).add(message);
        // Try to publish to the Server's RabbitMQ (which will be read by the ViralService)
        serverRabbitMQManager.publishTopicMessage(message);
    }

    public List<TopicMessage> readMessages(String type) {
        ConcurrentLinkedQueue<TopicMessage> messages = topicMessages.getOrDefault(type, new ConcurrentLinkedQueue<>());
        return new ArrayList<>(messages);
    }

    public void clearMessagesForTopic(String topic) {
        topicMessages.remove(topic);
    }

    public int size(String type) {
        ConcurrentLinkedQueue<TopicMessage> queue = topicMessages.get(type);
        return (queue != null) ? queue.size() : 0;
    }

    private void startGarbageCollector() {
        garbageCollectorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long currentTime = System.currentTimeMillis();
                    topicMessages.forEach((type, queue) -> {
                        queue.removeIf(message -> currentTime - message.getSentTime() > maxTimeout);
                    });
                    Thread.sleep(500);
                } catch (InterruptedException e) {
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
        if (garbageCollectorThread != null) {
            garbageCollectorThread.interrupt();
        }
    }
}
