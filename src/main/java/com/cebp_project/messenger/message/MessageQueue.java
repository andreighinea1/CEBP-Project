package com.cebp_project.messenger.message;

import com.cebp_project.rabbitmq.RabbitMQManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

public class MessageQueue {
    private static MessageQueue instance;
    private final BlockingQueue<Message> queue;
    private final RabbitMQManager rabbitMQManager;

    // Private constructor to handle exceptions from RabbitMQManager.getInstance()
    private MessageQueue(int maxSize) {
        this.queue = new LinkedBlockingQueue<>(maxSize);
        try {
            this.rabbitMQManager = RabbitMQManager.getInstance();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Unable to initialize RabbitMQManager", e);
        }
    }

    // Static method for getting the singleton instance
    public static synchronized MessageQueue getInstance() {
        if (instance == null) {
            instance = new MessageQueue(100); // Default size for the queue
        }
        return instance;
    }

    public void sendMessage(Message message) throws IllegalStateException {
        queue.add(message);
        try {
            rabbitMQManager.publishMessage(message); // Publish to RabbitMQ
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    public Message receiveMessage(String recipient) {
        Iterator<Message> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (recipient.equals(message.getRecipient())) {
                iterator.remove();
                return message;
            }
        }
        return null;
    }

    public List<Message> getAllMessages() {
        return new ArrayList<>(queue);
    }

    public Message poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}
