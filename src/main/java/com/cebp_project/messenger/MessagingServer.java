package com.cebp_project.messenger;

import com.cebp_project.messenger.client.Client;
import com.cebp_project.messenger.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessagingServer {
    private static final Logger logger = LoggerFactory.getLogger(MessagingServer.class);

    public static void main(String[] args) {
        logger.info("Starting MessagingServer");

        List<String> topics = Arrays.asList("topic1", "topic2");
        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        List<Thread> clientThreads = new ArrayList<>();
        for (String clientName : clientNames) {
            List<String> clientTopics = new ArrayList<>();
            // Randomly subscribe each client to one or more topics
            for (String topic : topics) {
                if (Math.random() > 0.5) {
                    clientTopics.add(topic);
                }
            }

            Client client = new Client(clientName, clientNames, server, clientTopics);
            Thread clientThread = new Thread(client);
            clientThreads.add(clientThread);
            clientThread.start();
        }

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
}
