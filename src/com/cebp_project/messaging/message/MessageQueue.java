package com.cebp_project.messaging.message;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private final BlockingQueue<Message> queue;

    public MessageQueue(int maxSize) {
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public void sendMessage(Message message) throws IllegalStateException {
        queue.add(message);
    }
    public Message pollViralMessage() {
        return queue.poll();
    }
    // In MessageQueue.java



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

    // Exposing the poll method of the BlockingQueue
    public Message poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}