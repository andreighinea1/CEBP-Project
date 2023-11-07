package com.cebp_project.messaging.topic;

public class TopicMessage {
    String type;
    String content;
    long timeout;

    public TopicMessage(String type, String content, long timeout) {
        this.type = type;
        this.content = content;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "TopicMessage{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
