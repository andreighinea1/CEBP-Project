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
        queue.add(message);
        rabbitMQManager.publishMessage(message); // Publish to ViralService's RabbitMQ
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
