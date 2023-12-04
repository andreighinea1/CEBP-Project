package com.cebp_project.messenger.topic;

import com.cebp_project.viral_service.ViralService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TopicOrchestrator {
    private static final TopicOrchestrator instance = new TopicOrchestrator(5000);
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<TopicMessage>> topicMessages;
    private final long maxTimeout;

    private TopicOrchestrator(long maxTimeout) {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentHashMap<>();
        startGarbageCollector();
    }

    public static void publishMessage(TopicMessage message) {
        instance.topicMessages.computeIfAbsent(message.getType(), k -> new ConcurrentLinkedQueue<>()).add(message);
        ViralService.getInstance().notifyNewMessage(); // Notify the Viral service
    }

    public static List<TopicMessage> readMessages(String type) {
        return new ArrayList<>(instance.topicMessages.getOrDefault(type, new ConcurrentLinkedQueue<>()));
    }

    public static List<TopicMessage> getAllMessages() {
        List<TopicMessage> allMessages = new ArrayList<>();
        for (ConcurrentLinkedQueue<TopicMessage> queue : instance.topicMessages.values()) {
            allMessages.addAll(queue);
        }
        return allMessages;
    }

    public static int size(String type) {
        ConcurrentLinkedQueue<TopicMessage> queue = instance.topicMessages.get(type);
        return (queue != null) ? queue.size() : 0;
    }

    private void startGarbageCollector() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);

                    long currentTime = System.currentTimeMillis();
                    instance.topicMessages.forEach((type, queue) -> {
                        queue.removeIf(message -> currentTime - message.getSentTime() > instance.maxTimeout);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }
}
