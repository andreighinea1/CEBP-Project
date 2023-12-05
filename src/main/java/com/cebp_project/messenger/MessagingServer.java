package com.cebp_project.messenger;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.server.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MessagingServer {
    // TODO-bia-3: This may be needed to be modified after implementing the RabbitMQ queue

    public static void main(String[] args) throws IOException, TimeoutException {
        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        for (String clientName : clientNames) {
            // Pass only the client name to the Client constructor
            Client client = new Client(clientName);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
    }
}
