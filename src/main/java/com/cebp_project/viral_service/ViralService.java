package com.cebp_project.viral_service;

// TODO-ale-last?: After the RabbitMQ is implemented, all these imports should be removed, and the DTO should be used
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.topic.TopicMessage;
import com.cebp_project.messenger.topic.TopicOrchestrator;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
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

    private final static String BROADCAST_QUEUE_NAME = "broadcast_queue";
    private final static String TOPIC_QUEUE_NAME = "topic_queue";
    private Connection connection;
    private Channel channel;

    private void setupRabbitMQ() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");  // Replace with the actual host
            // factory.setUsername("username");  // Uncomment if you have username
            // factory.setPassword("password");  // Uncomment if you have password
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Declare the queues
            channel.queueDeclare(BROADCAST_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(TOPIC_QUEUE_NAME, false, false, false, null);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
        }
    }
    public static ViralService getInstance() {
        return instance;
    }


    // Overloaded method to handle TopicMessage
    public void notifyNewMessage(Message message) throws IOException {
        if (channel == null) {
            setupRabbitMQ();  // Attempt to re-establish connection
            if (channel == null) {
                throw new IllegalStateException("RabbitMQ channel is not initialized.");
            }
        }
        String messageJson = convertMessageToJson(message);
        channel.basicPublish("", BROADCAST_QUEUE_NAME, null, messageJson.getBytes());
    }


    public void notifyNewTopicMessage(TopicMessage message) throws IOException {
        String messageJson = convertTopicMessageToJson(message); // Serialize the message
        channel.basicPublish("", TOPIC_QUEUE_NAME, null, messageJson.getBytes());
    }


    @Override
    public void run() {
        try {
            setupRabbitMQ();

            DeliverCallback broadcastCallback = (consumerTag, delivery) -> {
                String messageJson = new String(delivery.getBody(), "UTF-8");
                Message message = convertJsonToMessage(messageJson); // Deserialize the message
                processBroadcastMessage(message);
            };
            channel.basicConsume(BROADCAST_QUEUE_NAME, true, broadcastCallback, consumerTag -> {});

            DeliverCallback topicCallback = (consumerTag, delivery) -> {
                String messageJson = new String(delivery.getBody(), "UTF-8");
                TopicMessage message = convertJsonToTopicMessage(messageJson); // Deserialize the message
                processTopicMessage(message);
            };
            channel.basicConsume(TOPIC_QUEUE_NAME, true, topicCallback, consumerTag -> {});

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (connection != null) connection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    private String convertMessageToJson(Message message) {
        // Implement serialization logic, possibly using a library like Gson or Jackson
        return "";  // Placeholder
    }

    private Message convertJsonToMessage(String json) {
        // Implement deserialization logic
        return new Message("senderId","recipientId","Hello, this is a message!",16);  // Placeholder
    }

    private String convertTopicMessageToJson(TopicMessage message) {
        // Implement serialization logic
        return "";  // Placeholder
    }

    private TopicMessage convertJsonToTopicMessage(String json) {
        // Implement deserialization logic
        return new TopicMessage("someTopicType","This is the content of the topic message.");  // Placeholder
    }
    public void stopService() {
        try {
            if (channel != null) channel.close();
            if (connection != null) connection.close();
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
