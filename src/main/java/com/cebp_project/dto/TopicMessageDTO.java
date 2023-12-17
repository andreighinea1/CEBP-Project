package com.cebp_project.dto;

import com.cebp_project.messenger.topic.TopicMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * Data Transfer Object for topic message data.
 * Provides methods for serializing and deserializing topic message data,
 * along with methods to convert from and to {@link TopicMessage} objects.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicMessageDTO implements Serializable {
    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("sentTime")
    private long sentTime;

    /**
     * Constructs a new TopicMessageDTO instance.
     *
     * @param type     the type of the topic message
     * @param content  the content of the topic message
     * @param sentTime the time when the message was sent
     */
    private TopicMessageDTO(@JsonProperty("type") String type,
                            @JsonProperty("content") String content,
                            @JsonProperty("sentTime") long sentTime) {
        this.type = type;
        this.content = content;
        this.sentTime = sentTime;
    }

    /**
     * Converts a {@link TopicMessage} object to a TopicMessageDTO.
     *
     * @param message the topic message to convert
     * @return a new instance of TopicMessageDTO
     */
    public static TopicMessageDTO fromTopicMessage(TopicMessage message) {
        return new TopicMessageDTO(message.getType(), message.getContent(), message.getSentTime());
    }

    /**
     * Deserializes a JSON string to a TopicMessageDTO object.
     *
     * @param json the JSON string to deserialize
     * @return a TopicMessageDTO object or null if deserialization fails
     */
    public static TopicMessageDTO fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, TopicMessageDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serializes this TopicMessageDTO to a JSON string.
     *
     * @return a JSON representation of this TopicMessageDTO or null if serialization fails
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
        return "TopicMessageDTO{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
