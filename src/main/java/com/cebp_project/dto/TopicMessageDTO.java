package com.cebp_project.dto;

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

    // Constructor
    public TopicMessageDTO(@JsonProperty("type") String type,
                           @JsonProperty("content") String content,
                           @JsonProperty("sentTime") long sentTime) {
        this.type = type;
        this.content = content;
        this.sentTime = sentTime;
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

    // Getters
    public String getType() {
        return type;
    }

    // Setters
    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
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

    @Override
    public String toString() {
        return "TopicMessageDTO{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
