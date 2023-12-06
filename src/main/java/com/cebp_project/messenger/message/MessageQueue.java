package com.cebp_project.messenger.message;

import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

public class MessageQueue {
    private static final Logger logger = LoggerFactory.getLogger(MessageQueue.class);
    private static MessageQueue instance;
    private final BlockingQueue<Message> queue;
    private final RabbitMQManager rabbitMQManager;

    private MessageQueue(int maxSize) throws IOException, TimeoutException {
        this.queue = new LinkedBlockingQueue<>(maxSize);
        this.rabbitMQManager = RabbitMQManager.getInstance();
    }

    public static synchronized MessageQueue getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new MessageQueue(100); // Default size for the queue
        }
        return instance;
    }

    public void sendMessage(Message message) throws IllegalStateException, IOException {
        logger.info("Sending message from {} to {}", message.getSender(), message.getRecipient());
        queue.add(message);
        try {
            rabbitMQManager.publishMessage(message);
        } catch (IOException e) {
            logger.error("Failed to publish message to RabbitMQ", e);
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    public Message receiveMessage(String recipient) {
        logger.debug("Receiving message for {}", recipient);
        Iterator<Message> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (recipient.equals(message.getRecipient())) {
                iterator.remove();
                logger.info("Message received for {}: {}", recipient, message);
                return message;
            }
        }
        return null;
    }

    public List<Message> getAllMessages() {
        logger.debug("Getting all messages");
        return new ArrayList<>(queue);
    }

    public Message poll() {
        logger.debug("Polling message");
        return queue.poll();
    }

    public int size() {
        logger.debug("Checking queue size");
        return queue.size();
    }
}
