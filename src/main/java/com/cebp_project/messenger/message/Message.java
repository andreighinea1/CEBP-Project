package com.cebp_project.messenger.message;

import java.util.Objects;

public class Message {
    String sender;
    String recipient;
    String content;
    long timestamp;

    public Message(String sender, String recipient, String content, long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Message(String sender, String recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
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
