package com.cebp_project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageQueueDTO implements Serializable {
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

    // Method to deserialize JSON to MessageQueueDTO
    public static MessageQueueDTO fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, MessageQueueDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Getters
    public String getSender() {
        return sender;
    }

    // Setters
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Method to serialize MessageQueueDTO to JSON
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "MessageQueueDTO{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
