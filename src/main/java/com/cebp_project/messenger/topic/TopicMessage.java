package com.cebp_project.messenger.topic;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicMessage that)) return false;
        return getSentTime() == that.getSentTime() && Objects.equals(getType(), that.getType()) && Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentTime(), getType(), getContent());
    }
}
