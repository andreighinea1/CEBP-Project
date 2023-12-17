package com.cebp_project.messenger;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MessagingServer {
    private static final Logger logger = LoggerFactory.getLogger(MessagingServer.class);

    public static void main(String[] args) {
        logger.info("Starting MessagingServer");

        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        for (String clientName : clientNames) {
            // Pass all required parameters to the Client constructor
            Client client = new Client(clientName, clientNames, server);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
    }
}
