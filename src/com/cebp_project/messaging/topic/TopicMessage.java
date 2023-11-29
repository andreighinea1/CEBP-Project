package com.cebp_project.messaging.topic;

public class TopicMessage {
    String type;
    String content;
    private final long sentTime;

    public TopicMessage(String type, String content) {
        this.type = type;
        this.content = content;
        this.sentTime = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getSentTime() {
        return sentTime;
    }

    @Override
    public String toString() {
        return type + ": " + content;
    }
}
