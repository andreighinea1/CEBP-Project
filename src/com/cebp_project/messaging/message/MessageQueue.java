package com.cebp_project.messaging.message;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private BlockingQueue<Message> queue;
    private int maxSize;

    public MessageQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public void sendMessage(Message message) {
        if (queue.size() < maxSize) {
            queue.add(message);
        } else {
            queue.poll(); // Remove the oldest message
            queue.add(message); // Add the new message
        }
    }

    public Message receiveMessage(String recipient) {
        Iterator<Message> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (recipient.equals(message.recipient)) {
                iterator.remove();
                return message;
            }
        }
        return null;
    }

    public int size() {
        return queue.size();
    }
}