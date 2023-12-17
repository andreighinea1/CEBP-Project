package com.cebp_project.dto;

import com.cebp_project.messenger.message.Message;
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

    private MessageQueueDTO(@JsonProperty("sender") String sender,
                            @JsonProperty("recipient") String recipient,
                            @JsonProperty("content") String content,
                            @JsonProperty("timestamp") long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }


    public static MessageQueueDTO fromMessage(Message message) {
        return new MessageQueueDTO(message.getSender(), message.getRecipient(), message.getContent(), message.getTimestamp());
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
