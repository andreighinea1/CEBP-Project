package com.cebp_project.rabbitmq;

import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.dto.TopicMessageDTO;
import com.cebp_project.messenger.message.Message;
import com.cebp_project.messenger.topic.TopicMessage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQManager {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQManager.class);
    private static final String BROADCAST_QUEUE_NAME = "broadcast_queue";
    private static final String TOPIC_QUEUE_NAME = "topic_queue";
    private static RabbitMQManager instance;
    private Channel channel;
    private Connection connection;

    private RabbitMQManager() {
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
        factory.setUsername("guest");
        factory.setPassword("guest");

        try {
            logger.info("Attempting to establish RabbitMQ connection...");
            connection = factory.newConnection();
            channel = connection.createChannel();

            logger.info("Declaring queues...");
            channel.queueDeclare(BROADCAST_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(TOPIC_QUEUE_NAME, false, false, false, null);
            logger.info("Queues declared successfully.");
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to set up RabbitMQ: ", e);
            throw new RuntimeException(e);
        }
    }

    public void publishMessage(Message message) throws IOException {
        try {
            String messageJson = MessageQueueDTO.fromMessage(message).toJson();
            logger.debug("Publishing message to broadcast queue: {}", messageJson);
            channel.basicPublish("", BROADCAST_QUEUE_NAME, null, messageJson.getBytes());
        } catch (IOException e) {
            logger.error("Failed to publish message: ", e);
            throw e;
        }
    }

    public void publishTopicMessage(TopicMessage message) throws IOException {
        try {
            String messageJson = TopicMessageDTO.fromTopicMessage(message).toJson();
            logger.debug("Publishing message to topic queue: {}", messageJson);
            channel.basicPublish("", TOPIC_QUEUE_NAME, null, messageJson.getBytes());
        } catch (IOException e) {
            logger.error("Failed to publish topic message: ", e);
            throw e;
        }
    }

    private void consumeMessages(String queueName, Consumer<String> messageProcessor) throws IOException {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                logger.debug("Message received from {}: {}", queueName, message);
                messageProcessor.accept(message);
            };
            logger.debug("Starting to consume messages from {}", queueName);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            logger.error("Failed to consume messages from {}: ", queueName, e);
            throw e;
        }
    }

    public void consumeBroadcastMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(BROADCAST_QUEUE_NAME, messageProcessor);
    }

    public void consumeTopicMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(TOPIC_QUEUE_NAME, messageProcessor);
    }

    public void close() throws IOException, TimeoutException {
        try {
            if (channel != null) {
                logger.info("Closing RabbitMQ channel...");
                channel.close();
            }
            if (connection != null) {
                logger.info("Closing RabbitMQ connection...");
                connection.close();
            }
            logger.info("RabbitMQ resources closed successfully.");
        } catch (IOException | TimeoutException e) {
            logger.error("Error closing RabbitMQ resources: ", e);
            throw e;
        }
    }
}
