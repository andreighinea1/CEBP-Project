package com.cebp_project.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class TopicMessageDTO {
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

    // Setters
    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    // Method to serialize TopicMessageDTO to JSON
    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    // Method to deserialize JSON to TopicMessageDTO
    public static TopicMessageDTO fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, TopicMessageDTO.class);
    }
}
