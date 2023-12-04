package com.cebp_project.messenger;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.server.Server;

import java.util.Arrays;
import java.util.List;

public class MessagingServer {
    // TODO-bia-3: This may be needed to be modified after implementing the RabbitMQ queue

    public static void main(String[] args) {
        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        for (String clientName : clientNames) {
            // Pass all required parameters to the Client constructor
            Client client = new Client(clientName, MessageQueue.getInstance(), clientNames, server);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
    }
}
