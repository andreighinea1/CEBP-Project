package com.cebp_project.viral_service;
package com.cebp_project.messaging.viral;

// TODO-ale-last?: After the RabbitMQ is implemented, all these imports should be removed, and the DTO should be used
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViralService implements Runnable {
    // TODO-bia-1: Don't use semaphores anymore, just duplicate the msg and send it to RabbitMQ
    //  NOTE: Only use RabbitMQ for the connection between the Server and ViralService

    // TODO-ale-1: Make a DTO for transferring messages in the MessageQueue,
    //  and another one for the ones in TopicOrchestrator (this will be used to communicate with RabbitMQ)

    // TODO-deea-1: Make a main method to start this in a separate "admin" process

    private static final ViralService instance = new ViralService();
    private final ConcurrentHashMap<String, Integer> broadcastHashtagCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> topicHashtagCounts = new ConcurrentHashMap<>();
    private final Set<Message> processedMessages = ConcurrentHashMap.newKeySet();
    private final Set<TopicMessage> processedTopicMessages = ConcurrentHashMap.newKeySet();
    private final Semaphore newMessageSemaphore = new Semaphore(0);  // Used to indicate new messages

    public static ViralService getInstance() {
        return instance;
    }

    public void notifyNewMessage() {
        newMessageSemaphore.release();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                newMessageSemaphore.acquire();
                processBroadcastMessages();  // TODO-bia-2: These 2 channels would be RabbitMQ channels
                processTopicMessages();
                displayTrendingHashtags();
                newMessageSemaphore.drainPermits();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processBroadcastMessages() {
        for (Message message : MessageQueue.getInstance().getAllMessages()) {
            if (!processedMessages.contains(message)) {
                extractAndCountHashtags(message.getContent(), broadcastHashtagCounts);
                processedMessages.add(message);
            }
        }
    }

    private void processTopicMessages() {
        List<TopicMessage> topicMessages = TopicOrchestrator.getAllMessages();
        for (TopicMessage message : topicMessages) {
            if (!processedTopicMessages.contains(message)) {
                extractAndCountHashtags(message.getContent(), topicHashtagCounts);
                processedTopicMessages.add(message);
            }
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
        if (!broadcastHashtagCounts.isEmpty()) {
            displayHashtagCounts("Broadcast", broadcastHashtagCounts);
        }
        if (!topicHashtagCounts.isEmpty()) {
            displayHashtagCounts("Topic", topicHashtagCounts);
        }
    }

    private void displayHashtagCounts(String messageType, ConcurrentHashMap<String, Integer> hashtagCounts) {
        System.out.println("Trending Hashtags in " + messageType + " Messages:");
        hashtagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10) // Display top 10 hashtags
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}
