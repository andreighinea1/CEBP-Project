package com.cebp_project.messenger.message;

import java.util.Objects;

/**
 * Represents a message in the messaging system.
 */
public class Message {
    private String sender;
    private String recipient;
    private String content;
    private long timestamp;

    /**
     * Constructs a Message instance for decoding from RabbitMQ DTO.
     *
     * @param sender    The sender of the message.
     * @param recipient The recipient of the message.
     * @param content   The content of the message.
     * @param timestamp The time when the message was created.
     */
    public Message(String sender, String recipient, String content, long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Constructs a Message instance for sending.
     *
     * @param sender    The sender of the message.
     * @param recipient The recipient of the message.
     * @param content   The content of the message.
     */
    public Message(String sender, String recipient, String content) {
        this(sender, recipient, content, System.currentTimeMillis());
    }


    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message from '" + sender + "' to '" + recipient + "': " + content + " [at " + timestamp + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return getTimestamp() == message.getTimestamp() && Objects.equals(getSender(), message.getSender()) && Objects.equals(getRecipient(), message.getRecipient()) && Objects.equals(getContent(), message.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender(), getRecipient(), getContent(), getTimestamp());
    }
}
