package com.cebp_project.messenger;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.message.MessageQueue;
import com.cebp_project.messenger.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MessagingServer {
    private static final Logger logger = LoggerFactory.getLogger(MessagingServer.class);

    public static void main(String[] args) {
        logger.info("Starting MessagingServer");

        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        for (String clientName : clientNames) {
            try {
                // Pass all required parameters to the Client constructor
                Client client = new Client(clientName, MessageQueue.getInstance(), clientNames, server);
                Thread clientThread = new Thread(client);
                clientThread.start();
            } catch (IOException | TimeoutException e) {
                logger.error("Failed to start client: {}", clientName, e);
            }
        }
    }
}
