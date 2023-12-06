package com.cebp_project.messenger.client;

import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.rabbitmq.RabbitMQManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Client implements Runnable {
    private final String name;
    private final RabbitMQManager rabbitMQManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
////
    public Client(String name) throws IOException, TimeoutException {
        this.name = name;
        this.rabbitMQManager = RabbitMQManager.getInstance();
    }

    public void sendMessage(String content) throws IOException {
        MessageQueueDTO message = new MessageQueueDTO(this.name, "server", content, System.currentTimeMillis());
        String messageJson = rabbitMQManager.serializeObject(message);
        rabbitMQManager.publishMessage("client_to_server_queue", messageJson);
    }
////

    public void receiveTopicMessage(String topicMessage) {
        System.out.println(name + " received topic message: " + topicMessage);
    }

    @Override
    public void run() {
        try {
            rabbitMQManager.consumeMessages("server_to_client_queue", this::receiveMessage);

            // Simulate sending messages from a predefined list
            List<String> predefinedMessages = List.of("Hello", "How are you?", "Goodbye");

            for (String message : predefinedMessages) {
                sendMessage(message);
                Thread.sleep(1000);  // Optional: Add a delay between sending messages
            }

            // Add more simulated messages as needed

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(String messageJson) {
        try {
            MessageQueueDTO message = objectMapper.readValue(messageJson, MessageQueueDTO.class);
            System.out.println(name + " received: " + message.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String clientName = args.length > 0 ? args[0] : "DefaultClient";
            Client client = new Client(clientName);
            Thread clientThread = new Thread(client);
            clientThread.start();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
