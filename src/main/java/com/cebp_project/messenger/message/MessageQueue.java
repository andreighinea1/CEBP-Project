package com.cebp_project.messenger.message;

import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages a queue of messages for the messaging system.
 */
public class MessageQueue {
    private static final Logger logger = LoggerFactory.getLogger(MessageQueue.class);
    private final BlockingQueue<Message> queue;
    private final RabbitMQManager serverRabbitMQManager;

    /**
     * Constructs a MessageQueue with a specified maximum size and RabbitMQ manager.
     *
     * @param maxSize               The maximum size of the queue.
     * @param serverRabbitMQManager The RabbitMQ manager for message handling.
     */
    public MessageQueue(int maxSize, RabbitMQManager serverRabbitMQManager) {
        this.queue = new LinkedBlockingQueue<>(maxSize);
        this.serverRabbitMQManager = serverRabbitMQManager;
    }

    /**
     * Sends a message and publishes it to RabbitMQ.
     *
     * @param message The message to be sent.
     * @throws IllegalStateException If the queue is full.
     * @throws IOException           If an I/O error occurs in RabbitMQ handling.
     */
    public void sendMessage(Message message) throws IllegalStateException, IOException {
        logger.info("Sending message from {} to {}", message.getSender(), message.getRecipient());
        queue.add(message);
        serverRabbitMQManager.publishMessage(message);
    }

    /**
     * Polls a message from the queue.
     *
     * @return The polled message, or null if the queue is empty.
     */
    public Message poll() {
        logger.debug("Polling message");
        return queue.poll();
    }
}
