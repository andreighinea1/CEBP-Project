package com.cebp_project.messenger.client;

import com.cebp_project.dto.MessageQueueDTO;
import com.cebp_project.rabbitmq.RabbitMQManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client implements Runnable {
    private final String name;
    private final RabbitMQManager rabbitMQManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Client(String name) throws IOException, TimeoutException {
        this.name = name;
        this.rabbitMQManager = new RabbitMQManager("client_to_server_queue", "server_to_client_queue");
    }

    public void sendMessage(String content) throws IOException {
        MessageQueueDTO message = new MessageQueueDTO(this.name, "server", content, System.currentTimeMillis());
        rabbitMQManager.publishMessage(message);
    }

    public void receiveTopicMessage(String topicMessage) {
        System.out.println(name + " received topic message: " + topicMessage);
    }

    @Override
    public void run() {
        try {
            rabbitMQManager.consumeMessages("server_to_client_queue", this::receiveMessage);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter message (or 'exit' to quit): ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                sendMessage(input);
            }
        } catch (IOException e) {
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
