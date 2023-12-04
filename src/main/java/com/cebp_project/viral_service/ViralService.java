package com.cebp_project.viral_service;

import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.rabbitmq.RabbitMQManager;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViralService implements Runnable {
    private final ConcurrentHashMap<String, Integer> broadcastHashtagCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> topicHashtagCounts = new ConcurrentHashMap<>();
    private final Set<Message> processedMessages = ConcurrentHashMap.newKeySet();
    private final Set<TopicMessage> processedTopicMessages = ConcurrentHashMap.newKeySet();

    private final static String BROADCAST_QUEUE_NAME = "broadcast_queue";
    private final static String TOPIC_QUEUE_NAME = "topic_queue";
    private final RabbitMQManager rabbitMQManager;

    public ViralService() throws IOException, TimeoutException {
        rabbitMQManager = new RabbitMQManager(BROADCAST_QUEUE_NAME, TOPIC_QUEUE_NAME);
    }

    @Override
    public void run() {
        try {
            rabbitMQManager.consumeMessages(BROADCAST_QUEUE_NAME, this::processBroadcastMessageJson);
            rabbitMQManager.consumeMessages(TOPIC_QUEUE_NAME, this::processTopicMessageJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processBroadcastMessageJson(String messageJson) {
        Message message = convertJsonToMessage(messageJson);
        if (message != null) {
            processBroadcastMessage(message);
        }
    }

    private void processTopicMessageJson(String messageJson) {
        TopicMessage message = convertJsonToTopicMessage(messageJson);
        if (message != null) {
            processTopicMessage(message);
        }
    }

    private Message convertJsonToMessage(String json) {
        MessageQueueDTO dto = MessageQueueDTO.fromJson(json);
        if (dto == null) {
            return null;
        }
        return new Message(dto.getSender(), dto.getRecipient(), dto.getContent(), dto.getTimestamp());
    }

    private TopicMessage convertJsonToTopicMessage(String json) {
        // Implement deserialization logic for TopicMessage
        // Placeholder - replace with actual implementation
        return new TopicMessage("someTopicType", "This is the content of the topic message.");
    }

    public void stopService() {
        try {
            rabbitMQManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processBroadcastMessage(Message message) {
        if (!processedMessages.contains(message)) {
            extractAndCountHashtags(message.getContent(), broadcastHashtagCounts);
            processedMessages.add(message);
        }
        // Additional processing...
    }

    private void processTopicMessage(TopicMessage message) {
        if (!processedTopicMessages.contains(message)) {
            extractAndCountHashtags(message.getContent(), topicHashtagCounts);
            processedTopicMessages.add(message);
        }
        // Additional processing...
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
