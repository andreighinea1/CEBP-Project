import com.cebp_project.messaging.client.Client;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.server.Server;
import com.cebp_project.messaging.viral.ViralService;

import java.util.Arrays;
import java.util.List;

public class MessagingServer {
    public static void main(String[] args) {
        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        MessageQueue messageQueue = MessageQueue.getInstance();
        Server server = new Server(messageQueue);
        Thread serverThread = new Thread(server);
        serverThread.start();

        for (String clientName : clientNames) {
            // Pass all required parameters to the Client constructor
            Client client = new Client(clientName, messageQueue, clientNames, server);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }

        Thread viralThread = new Thread(ViralService.getInstance());
        viralThread.start();
    }
}
