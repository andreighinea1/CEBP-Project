package com.cebp_project.messaging.message;

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
}
