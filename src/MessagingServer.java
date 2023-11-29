

import com.cebp_project.messaging.client.Client;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.server.Server;
import com.cebp_project.messaging.viral.Viral;

import java.util.Arrays;
import java.util.List;

public class MessagingServer {
    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue(100);
        List<String> clientNames = Arrays.asList("Client 1", "Client 2", "Client 3");

        Thread serverThread = new Thread(new Server(messageQueue));
        serverThread.start();

        for (String clientName : clientNames) {
            Thread clientThread = new Thread(new Client(clientName, messageQueue, clientNames));
            clientThread.start();
        }

        Thread viralThread = new Thread(new Viral(messageQueue));
        viralThread.start();
    }
}
