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

import static com.cebp_project.messenger.constants.Constants.RABBITMQ_BROADCAST_QUEUE_NAME;
import static com.cebp_project.messenger.constants.Constants.RABBITMQ_TOPIC_QUEUE_NAME;

/**
 * Manages RabbitMQ connections and message operations.
 * This class provides methods to publish and consume messages from RabbitMQ queues.
 */
public class RabbitMQManager {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQManager.class);
    private Channel channel;
    private Connection connection;

    /**
     * Initializes and sets up RabbitMQ connection and channels.
     */
    public RabbitMQManager() {
        setupRabbitMQ();
    }

    /**
     * Sets up the RabbitMQ connection and channel, declaring necessary queues.
     * Throws RuntimeException if setup fails.
     */
    private void setupRabbitMQ() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        try {
            logger.info("Attempting to establish RabbitMQ connection...");
            connection = factory.newConnection();
            channel = connection.createChannel();

            logger.info("Declaring queues...");
            channel.queueDeclare(RABBITMQ_BROADCAST_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(RABBITMQ_TOPIC_QUEUE_NAME, false, false, false, null);
            logger.info("Queues declared successfully.");
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to set up RabbitMQ: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Publishes a message to the broadcast queue in RabbitMQ.
     *
     * @param message The message to be published.
     * @throws IOException if there's an issue with message publishing.
     */
    public void publishMessage(Message message) throws IOException {
        try {
            String messageJson = MessageQueueDTO.fromMessage(message).toJson();
            logger.debug("Publishing message to broadcast queue: {}", messageJson);
            channel.basicPublish("", RABBITMQ_BROADCAST_QUEUE_NAME, null, messageJson.getBytes());
        } catch (IOException e) {
            logger.error("Failed to publish broadcast message {} to RabbitMQ", message, e);
            throw e;
        }
    }

    /**
     * Publishes a topic message to the topic queue in RabbitMQ.
     *
     * @param message The topic message to be published.
     * @throws IOException if there's an issue with topic message publishing.
     */
    public void publishTopicMessage(TopicMessage message) throws IOException {
        try {
            String messageJson = TopicMessageDTO.fromTopicMessage(message).toJson();
            logger.debug("Publishing message to the topic queue: {}", messageJson);
            channel.basicPublish("", RABBITMQ_TOPIC_QUEUE_NAME, null, messageJson.getBytes());
        } catch (IOException e) {
            logger.error("Failed to publish topic message {} to RabbitMQ", message, e);
            throw e;
        }
    }

    /**
     * Consumes messages from a specified queue.
     *
     * @param queueName        The name of the RabbitMQ queue to consume from.
     * @param messageProcessor The function to process the consumed messages.
     * @throws IOException if there's an issue with message consumption.
     */
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

    /**
     * Starts consuming messages from the broadcast queue.
     *
     * @param messageProcessor The function to process the consumed broadcast messages.
     * @throws IOException if there's an issue with message consumption.
     */
    public void consumeBroadcastMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(RABBITMQ_BROADCAST_QUEUE_NAME, messageProcessor);
    }

    /**
     * Starts consuming messages from the topic queue.
     *
     * @param messageProcessor The function to process the consumed topic messages.
     * @throws IOException if there's an issue with message consumption.
     */
    public void consumeTopicMessages(Consumer<String> messageProcessor) throws IOException {
        consumeMessages(RABBITMQ_TOPIC_QUEUE_NAME, messageProcessor);
    }

    /**
     * Closes the RabbitMQ channel and connection, releasing resources.
     *
     * @throws IOException      if there's an issue closing the channel or connection.
     * @throws TimeoutException if a network timeout occurs.
     */
    public void close() throws IOException, TimeoutException {
        try {
            if (channel != null) {
                logger.info("Closing RabbitMQ channel...");
                channel.close();
                channel = null;
            }
            if (connection != null) {
                logger.info("Closing RabbitMQ connection...");
                connection.close();
                connection = null;
            }
            logger.info("RabbitMQ resources closed successfully.");
        } catch (IOException | TimeoutException e) {
            logger.error("Error closing RabbitMQ resources: ", e);
            throw e;
        }
    }
}
