package com.cebp_project.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class MessageQueueDTO {
    @JsonProperty("sender")
    private String sender;

    @JsonProperty("recipient")
    private String recipient;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private long timestamp;

    // Constructor
    public MessageQueueDTO(@JsonProperty("sender") String sender,
                           @JsonProperty("recipient") String recipient,
                           @JsonProperty("content") String content,
                           @JsonProperty("timestamp") long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters
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

    // Setters
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Method to serialize MessageQueueDTO to JSON
    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    // Method to deserialize JSON to MessageQueueDTO
    public static MessageQueueDTO fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, MessageQueueDTO.class);
    }
}
