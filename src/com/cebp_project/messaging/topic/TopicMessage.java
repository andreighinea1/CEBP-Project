package com.cebp_project.messaging.topic;

public class TopicMessage {
    String type;
    String content;
    long timeout;

    public TopicMessage(String type, String content, long timeout) {
        this.type = type;
        this.content = content;
        this.timeout = timeout;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return type + ": " + content;
    }
}
