package com.cebp_project.messaging.viral;

import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.topic.TopicMessage;
import com.cebp_project.messaging.topic.TopicOrchestrator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Viral implements Runnable {
    private final ConcurrentHashMap<String, Integer> broadcastHashtagCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> topicHashtagCounts = new ConcurrentHashMap<>();
    private final MessageQueue messageQueue;

    public Viral(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            processBroadcastMessages();
            processTopicMessages();
            displayTrendingHashtags();
            try {
                Thread.sleep(5000); // Refresh every 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processBroadcastMessages() {
        Message message;
        while ((message = messageQueue.poll()) != null) {
            extractAndCountHashtags(message.getContent(), broadcastHashtagCounts);
        }
    }

    private void processTopicMessages() {
        // Assuming TopicOrchestrator has a method to retrieve all messages
        List<TopicMessage> topicMessages = TopicOrchestrator.getAllMessages();
        for (TopicMessage message : topicMessages) {
            extractAndCountHashtags(message.getContent(), topicHashtagCounts);
        }
    }

    private void extractAndCountHashtags(String content, ConcurrentHashMap<String, Integer> hashtagMap) {
        Matcher matcher = Pattern.compile("#\\w+").matcher(content);
        while (matcher.find()) {
            String hashtag = matcher.group();
            hashtagMap.merge(hashtag, 1, Integer::sum);
        }
    }

    private void displayTrendingHashtags() {
        displayHashtagCounts("Broadcast", broadcastHashtagCounts);
        displayHashtagCounts("Topic", topicHashtagCounts);
    }

    private void displayHashtagCounts(String messageType, ConcurrentHashMap<String, Integer> hashtagCounts) {
        System.out.println("Trending Hashtags in " + messageType + " Messages:");
        hashtagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10) // Display top 10 hashtags
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}

