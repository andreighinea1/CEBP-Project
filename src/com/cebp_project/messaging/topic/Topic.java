package com.cebp_project.messaging.topic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Topic {
    private ConcurrentLinkedQueue<TopicMessage> topicMessages;
    private long maxTimeout;

    public Topic(long maxTimeout) {
        this.maxTimeout = maxTimeout;
        this.topicMessages = new ConcurrentLinkedQueue<>();
    }

    public void publishMessage(TopicMessage message) {
        topicMessages.add(message);
    }

    public List<TopicMessage> readMessages(String type) {
        long currentTime = System.currentTimeMillis();
        List<TopicMessage> result = new ArrayList<>();
        Iterator<TopicMessage> iterator = topicMessages.iterator();
        while (iterator.hasNext()) {
            TopicMessage message = iterator.next();
            if (currentTime - message.timeout > maxTimeout) {
                iterator.remove();
            } else if (type.equals(message.type)) {
                result.add(message);
            }
        }
        return result;
    }

    public int size() {
        return topicMessages.size();
    }
}
