package com.cebp_project.messenger;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The MessagingServer class is the entry point for starting the messaging server.
 * It initiates the server and client threads and handles the orderly shutdown of these components.
 */
public class MessagingServer {
    private static final Logger logger = LoggerFactory.getLogger(MessagingServer.class);

    /**
     * The main method to start the Messaging Server.
     *
     * @param args Command line arguments (not used in this implementation).
     */
    public static void main(String[] args) {
        logger.info("Starting MessagingServer");

        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        List<Thread> clientThreads = startClientThreads(clientNames, server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down MessagingServer");
            server.stopServer();
            clientThreads.forEach(Thread::interrupt);
            try {
                serverThread.join();
                for (Thread clientThread : clientThreads) {
                    clientThread.join();
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted during shutdown", e);
                Thread.currentThread().interrupt();
            }
            logger.info("MessagingServer shutdown complete");
        }));
    }

    /**
     * Starts client threads for each client name provided.
     * It ensures that specific clients are subscribed to certain topics.
     *
     * @param clientNames A list of client names to start threads for.
     * @param server      The server instance to which the clients will connect.
     * @return A list of threads representing the clients.
     */
    private static List<Thread> startClientThreads(List<String> clientNames, Server server) {
        List<Thread> clientThreads = new ArrayList<>();
        for (String clientName : clientNames) {
            // All clients subscribe to topic1
            List<String> clientTopics = new ArrayList<>(List.of("topic1"));

            // Client 1 and 2 are always subscribed to topic2
            if ("Client 1".equals(clientName) || "Client 2".equals(clientName)) {
                clientTopics.add("topic2");
            }

            Client client = new Client(clientName, clientNames, server, clientTopics);
            Thread clientThread = new Thread(client);
            clientThreads.add(clientThread);
            clientThread.start();
        }
        return clientThreads;
    }
}
