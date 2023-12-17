package com.cebp_project.services;

import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.dto.TopicMessageDTO;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ViralService is responsible for consuming messages from RabbitMQ, processing them,
 * and identifying trending hashtags in both broadcast and topic messages.
 */
public class ViralService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ViralService.class);
    private final ConcurrentHashMap<String, Integer> broadcastHashtagCounts;
    private final ConcurrentHashMap<String, Integer> topicHashtagCounts;
    private final RabbitMQManager rabbitMQManager;
    private volatile boolean running = true;  // Flag to control the running of the service

    /**
     * Constructs a new ViralService.
     */
    public ViralService() {
        this.broadcastHashtagCounts = new ConcurrentHashMap<>();
        this.topicHashtagCounts = new ConcurrentHashMap<>();
        this.rabbitMQManager = new RabbitMQManager();
        logger.info("ViralService properly started.");
    }

    /**
     * The entry point of the ViralService.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        ViralService viralService = new ViralService();
        Thread viralThread = new Thread(viralService);
        viralThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Stopping ViralService.");
            viralService.stopService();
        }));

        try {
            viralThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Main thread interrupted", e);
        }
    }

    /**
     * The main run method of the service. It sets up message consumption and continuously
     * displays trending hashtags until the service is stopped.
     */
    @Override
    public void run() {
        try {
            rabbitMQManager.consumeBroadcastMessages(this::processBroadcastMessageJson);
            rabbitMQManager.consumeTopicMessages(this::processTopicMessageJson);
        } catch (IOException e) {
            logger.error("Error setting up message consumption in ViralService", e);
            return;
        }

        try {
            while (running) {
                displayTrendingHashtags();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.error("ViralService interrupted", e);
        } finally {
            stopService();
        }
    }

    /**
     * Processes a broadcast message in JSON format.
     *
     * @param messageJson The broadcast message in JSON format.
     */
    private void processBroadcastMessageJson(String messageJson) {
        Message message = convertJsonToMessage(messageJson);
        if (message != null) {
            processBroadcastMessage(message);
        }
    }

    /**
     * Processes a topic message in JSON format.
     *
     * @param messageJson The topic message in JSON format.
     */
    private void processTopicMessageJson(String messageJson) {
        TopicMessage message = convertJsonToTopicMessage(messageJson);
        if (message != null) {
            processTopicMessage(message);
        }
    }

    /**
     * Converts a JSON string to a Message object.
     *
     * @param json The JSON string representing the message.
     * @return The Message object or null if conversion fails.
     */
    private Message convertJsonToMessage(String json) {
        MessageQueueDTO dto = MessageQueueDTO.fromJson(json);
        if (dto == null) {
            logger.error("Failed to deserialize message from JSON");
            return null;
        }
        return new Message(dto.getSender(), dto.getRecipient(), dto.getContent(), dto.getTimestamp());
    }

    /**
     * Converts a JSON string to a TopicMessage object.
     *
     * @param json The JSON string representing the topic message.
     * @return The TopicMessage object or null if conversion fails.
     */
    private TopicMessage convertJsonToTopicMessage(String json) {
        TopicMessageDTO dto = TopicMessageDTO.fromJson(json);
        if (dto == null) {
            logger.error("Failed to deserialize topic message from JSON");
            return null;
        }
        return new TopicMessage(dto.getType(), dto.getContent(), dto.getSentTime());
    }

    /**
     * Processes a broadcast message to extract and count hashtags.
     *
     * @param message The broadcast message to process.
     */
    private void processBroadcastMessage(Message message) {
        extractAndCountHashtags(message.getContent(), broadcastHashtagCounts);
    }

    /**
     * Processes a topic message to extract and count hashtags.
     *
     * @param message The topic message to process.
     */
    private void processTopicMessage(TopicMessage message) {
        extractAndCountHashtags(message.getContent(), topicHashtagCounts);
    }

    /**
     * Extracts and counts hashtags from a given content string.
     *
     * @param content    The content from which to extract hashtags.
     * @param hashtagMap The map to store and count hashtags.
     */
    private void extractAndCountHashtags(String content, ConcurrentHashMap<String, Integer> hashtagMap) {
        Matcher matcher = Pattern.compile("#\\w+").matcher(content);
        while (matcher.find()) {
            String hashtag = matcher.group();
            hashtagMap.merge(hashtag, 1, Integer::sum);
        }
    }

    /**
     * Displays the top trending hashtags.
     */
    private void displayTrendingHashtags() {
        if (!broadcastHashtagCounts.isEmpty()) {
            displayHashtagCounts("Broadcast", broadcastHashtagCounts);
        }
        if (!topicHashtagCounts.isEmpty()) {
            displayHashtagCounts("Topic", topicHashtagCounts);
        }
    }

    /**
     * Displays hashtag counts for a specific message type.
     *
     * @param messageType   The type of message (e.g., "Broadcast" or "Topic").
     * @param hashtagCounts The map containing hashtag counts.
     */
    private void displayHashtagCounts(String messageType, ConcurrentHashMap<String, Integer> hashtagCounts) {
        logger.info("Trending Hashtags in {} Messages:", messageType);
        hashtagCounts.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(10)
                .forEach(entry -> logger.info("{}: {}", entry.getKey(), entry.getValue()));
    }

    /**
     * Stops the ViralService, closing any open resources.
     */
    public void stopService() {
        logger.info("Stopping ViralService.");
        running = false;  // Set flag to false in order to stop the service
        try {
            if (rabbitMQManager != null) {
                rabbitMQManager.close();
            }
        } catch (IOException | TimeoutException e) {
            logger.error("Error closing RabbitMQManager", e);
        }
    }
}
