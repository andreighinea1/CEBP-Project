package com.cebp_project.dto;

import com.cebp_project.messenger.topic.TopicMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicMessageDTO implements Serializable {
    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("sentTime")
    private long sentTime;

    private TopicMessageDTO(@JsonProperty("type") String type,
                            @JsonProperty("content") String content,
                            @JsonProperty("sentTime") long sentTime) {
        this.type = type;
        this.content = content;
        this.sentTime = sentTime;
    }


    public static TopicMessageDTO fromTopicMessage(TopicMessage message) {
        return new TopicMessageDTO(message.getType(), message.getContent(), message.getSentTime());
    }

    // Method to deserialize JSON to TopicMessageDTO
    public static TopicMessageDTO fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, TopicMessageDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to serialize TopicMessageDTO to JSON
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
