package com.cebp_project.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQManager {
    private static RabbitMQManager instance;
    private final Channel channel;
    private final Connection connection;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RabbitMQManager() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Replace with actual host
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static synchronized RabbitMQManager getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RabbitMQManager();
        }
        return instance;
    }

    public void publishMessage(String queueName, String messageJson) throws IOException {
        channel.basicPublish("", queueName, null, messageJson.getBytes());
    }

    public void consumeMessages(String queueName, Consumer<String> messageProcessor) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            messageProcessor.accept(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }

    public String serializeObject(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    public <T> T deserializeObject(String json, Class<T> objectType) throws IOException {
        return objectMapper.readValue(json, objectType);
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null) channel.close();
        if (connection != null) connection.close();
    }
}