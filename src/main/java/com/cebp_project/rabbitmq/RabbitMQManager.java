package com.cebp_project.rabbitmq;
/////
import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.topic.TopicMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQManager {
    private final String broadcastQueueName;
    private final String topicQueueName;
    private Channel channel;
    private Connection connection;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RabbitMQManager(String broadcastQueueName, String topicQueueName) throws IOException, TimeoutException {
        this.broadcastQueueName = broadcastQueueName;
        this.topicQueueName = topicQueueName;
        setupRabbitMQ();
    }

    private void setupRabbitMQ() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Replace with the actual host
        // factory.setUsername("username");  // Uncomment if you have username
        // factory.setPassword("password");  // Uncomment if you have password
        connection = factory.newConnection();
        channel = connection.createChannel();

        // Declare the queues
        channel.queueDeclare(broadcastQueueName, false, false, false, null);
        channel.queueDeclare(topicQueueName, false, false, false, null);
    }

    public void publishMessage(MessageQueueDTO message) throws IOException {
        // Convert the MessageQueueDTO object to JSON
        String messageJson = objectMapper.writeValueAsString(message);
        // Publish the message
        channel.basicPublish("", broadcastQueueName, null, messageJson.getBytes());
    }


    public void publishTopicMessage(TopicMessage message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        channel.basicPublish("", topicQueueName, null, messageJson.getBytes());
    }

    public void consumeMessages(String queueName, Consumer<String> messageProcessor) throws IOException {
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

    // Helper methods for message conversion (if needed)
    // ...
}