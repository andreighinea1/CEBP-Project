package com.cebp_project.rabbitmq;

import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.topic.TopicMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQManager {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQManager.class);
    private static final String BROADCAST_QUEUE_NAME = "broadcast_queue";
    private static final String TOPIC_QUEUE_NAME = "topic_queue";
    private static RabbitMQManager instance;
    private final ObjectMapper objectMapper;
    private Channel channel;
    private Connection connection;

    private RabbitMQManager() {
        this.objectMapper = new ObjectMapper();
        setupRabbitMQ();
    }

    public static synchronized RabbitMQManager getInstance() {
        if (instance == null) {
            instance = new RabbitMQManager();
        }
        return instance;
    }

    private void setupRabbitMQ() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");  // Replace with the actual host
        // factory.setUsername("username");
        // factory.setPassword("password");

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Declare the queues
            channel.queueDeclare(BROADCAST_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(TOPIC_QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishMessage(Message message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        channel.basicPublish("", BROADCAST_QUEUE_NAME, null, messageJson.getBytes());
    }

    public void publishTopicMessage(TopicMessage message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        channel.basicPublish("", TOPIC_QUEUE_NAME, null, messageJson.getBytes());
    }

    private void consumeMessages(String queueName, Consumer<String> messageProcessor) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            messageProcessor.accept(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    public void consumeBroadcastMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(BROADCAST_QUEUE_NAME, messageProcessor);
    }

    public void consumeTopicMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(TOPIC_QUEUE_NAME, messageProcessor);
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null) channel.close();
        if (connection != null) connection.close();
    }
}
