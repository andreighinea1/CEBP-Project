package com.cebp_project.messaging.message;

import com.cebp_project.messaging.topic.TopicMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private final BlockingQueue<Message> queue;

    public MessageQueue(int maxSize) {
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public void sendMessage(Message message) throws IllegalStateException {
        queue.add(message);
        viralService.notifyNewMessage(); // Notify the Viral service
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