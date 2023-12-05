package com.cebp_project.rabbitmq;

import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.topic.TopicMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQManager {
    private static RabbitMQManager instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Channel channel;
    private Connection connection;

    private static final String BROADCAST_QUEUE_NAME = "broadcast_queue";
    private static final String TOPIC_QUEUE_NAME = "topic_queue";

    private RabbitMQManager() throws IOException, TimeoutException {
        setupRabbitMQ();
    }

    public static synchronized RabbitMQManager getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RabbitMQManager();
        }
        return instance;
    }

    private void setupRabbitMQ() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(BROADCAST_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(TOPIC_QUEUE_NAME, false, false, false, null);
    }

    public void publishMessage(Message message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        channel.basicPublish("", BROADCAST_QUEUE_NAME, null, messageJson.getBytes());
    }

    public void publishTopicMessage(TopicMessage message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        channel.basicPublish("", TOPIC_QUEUE_NAME, null, messageJson.getBytes());
    }

    public void consumeBroadcastMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(BROADCAST_QUEUE_NAME, messageProcessor);
    }

    public void consumeTopicMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(TOPIC_QUEUE_NAME, messageProcessor);
    }

    private void consumeMessages(String queueName, Consumer<String> messageProcessor) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            messageProcessor.accept(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null) channel.close();
        if (connection != null) connection.close();
    }
}