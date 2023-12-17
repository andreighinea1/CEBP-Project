package com.cebp_project.messenger.topic;

public class TopicMessage {
    private final long sentTime;
    String type;
    String content;

    public TopicMessage(String type, String content) {
        this.type = type;
        this.content = content;
        this.sentTime = System.currentTimeMillis();
    }

    public TopicMessage(String type, String content, long sentTime) {
        this.type = type;
        this.content = content;
        this.sentTime = sentTime;
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
