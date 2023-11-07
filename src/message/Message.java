package message;

public class Message {
    String recipient;
    String content;
    long timestamp;

    public Message(String recipient, String content, long timestamp) {
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
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
        return "Message{" +
                "recipient='" + recipient + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
