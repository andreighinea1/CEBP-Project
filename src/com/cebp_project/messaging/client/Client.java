package com.cebp_project.messaging.client;

import com.cebp_project.messaging.message.Message;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.topic.Topic;
import com.cebp_project.messaging.topic.TopicMessage;

public class Client implements Runnable {
    private String name;
    private MessageQueue messageQueue;
    private Topic topic;

    public Client(String name, MessageQueue messageQueue, Topic topic) {
        this.name = name;
        this.messageQueue = messageQueue;
        this.topic = topic;
    }

    @Override
    public void run() {
        // Simulating sending and receiving messages
        messageQueue.sendMessage(new Message(name, "Hello from " + name, System.currentTimeMillis()));
        System.out.println(name + " received: " + messageQueue.receiveMessage(name).getContent());

        // Simulating topic interactions
        topic.publishMessage(new TopicMessage("type1", "Topic message from " + name, 1000));
        System.out.println(name + " topic messages: " + topic.readMessages("type1"));
    }
}
