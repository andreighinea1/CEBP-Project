package com.cebp_project.dto;

import com.cebp_project.messenger.message.Message;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * Data Transfer Object for message data in the message queue.
 * This class provides methods for serializing and deserializing message data,
 * along with methods to convert from and to {@link Message} objects.
 */
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

    /**
     * Constructs a new MessageQueueDTO instance.
     *
     * @param sender    the sender of the message
     * @param recipient the intended recipient of the message
     * @param content   the content of the message
     * @param timestamp the timestamp when the message was created
     */
    private MessageQueueDTO(@JsonProperty("sender") String sender,
                            @JsonProperty("recipient") String recipient,
                            @JsonProperty("content") String content,
                            @JsonProperty("timestamp") long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Converts a {@link Message} object to a MessageQueueDTO.
     *
     * @param message the message to convert
     * @return a new instance of MessageQueueDTO
     */
    public static MessageQueueDTO fromMessage(Message message) {
        return new MessageQueueDTO(message.getSender(), message.getRecipient(), message.getContent(), message.getTimestamp());
    }

    /**
     * Deserializes a JSON string to a MessageQueueDTO object.
     *
     * @param json the JSON string to deserialize
     * @return a MessageQueueDTO object or null if deserialization fails
     */
    public static MessageQueueDTO fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, MessageQueueDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serializes this MessageQueueDTO to a JSON string.
     *
     * @return a JSON representation of this MessageQueueDTO or null if serialization fails
     */
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
