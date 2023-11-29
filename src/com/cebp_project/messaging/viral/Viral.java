package com.cebp_project.messaging.viral;

import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.message.MessageQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Viral implements Runnable {
    private final ConcurrentHashMap<String, Integer> hashtagCounts = new ConcurrentHashMap<>();
    private final MessageQueue messageQueue;

    public Viral(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            processMessages();
            displayTrendingHashtags();
            try {
                Thread.sleep(5000); // Refresh every 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processMessages() {
        Message message;
        while ((message = messageQueue.pollViralMessage()) != null) {
            extractAndCountHashtags(message.getContent());
        }
    }

    private void extractAndCountHashtags(String content) {
        Matcher matcher = Pattern.compile("#\\w+").matcher(content);
        while (matcher.find()) {
            String hashtag = matcher.group();
            hashtagCounts.merge(hashtag, 1, Integer::sum);
        }
    }

    private void displayTrendingHashtags() {
        System.out.println("Trending Hashtags:");
        hashtagCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10) // Display top 10 hashtags
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}
