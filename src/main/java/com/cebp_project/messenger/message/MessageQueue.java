package com.cebp_project.messenger.message;

import com.cebp_project.rabbitmq.RabbitMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private static final Logger logger = LoggerFactory.getLogger(MessageQueue.class);
    private final BlockingQueue<Message> queue;
    private final RabbitMQManager serverRabbitMQManager;

    public MessageQueue(int maxSize, RabbitMQManager serverRabbitMQManager) {
        this.queue = new LinkedBlockingQueue<>(maxSize);
        this.serverRabbitMQManager = serverRabbitMQManager;
    }


    public void sendMessage(Message message) throws IllegalStateException, IOException {
        logger.info("Sending message from {} to {}", message.getSender(), message.getRecipient());
        queue.add(message);
        // Try to publish to the Server's RabbitMQ (which will be read by the ViralService)
        serverRabbitMQManager.publishMessage(message);
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
