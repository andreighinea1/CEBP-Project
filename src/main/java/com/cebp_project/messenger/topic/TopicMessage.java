package com.cebp_project.messenger.topic;

import java.util.Objects;

/**
 * Represents a topic message in the messaging system.
 */
public class TopicMessage {
    private final long sentTime;
    private String type;
    private String content;

    /**
     * Constructs a TopicMessage instance for decoding from RabbitMQ DTO.
     *
     * @param type     The type or category of the topic message.
     * @param content  The content of the topic message.
     * @param sentTime The time when the topic message was created.
     */
    public TopicMessage(String type, String content, long sentTime) {
        this.type = type;
        this.content = content;
        this.sentTime = sentTime;
    }

    /**
     * Constructs a TopicMessage instance for sending.
     *
     * @param type    The type or category of the topic message.
     * @param content The content of the topic message.
     */
    public TopicMessage(String type, String content) {
        this(type, content, System.currentTimeMillis());
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
