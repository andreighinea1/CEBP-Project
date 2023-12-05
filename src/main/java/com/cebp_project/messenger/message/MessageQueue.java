package com.cebp_project.messenger.message;

import com.cebp_project.viral_service.ViralService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private static final MessageQueue instance = new MessageQueue(100);
    private final BlockingQueue<Message> queue;

    private MessageQueue(int maxSize) {
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public static MessageQueue getInstance() {
        return instance;
    }

    public void sendMessage(Message message) throws IllegalStateException, IOException {
        queue.add(message);
        ViralService.getInstance().notifyNewMessage(message); // Notify the Viral service
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