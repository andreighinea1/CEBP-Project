import com.cebp_project.messaging.client.Client;
import com.cebp_project.messaging.message.MessageQueue;
import com.cebp_project.messaging.server.Server;
import com.cebp_project.messaging.topic.Topic;

public class MessagingServer {
    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue(10);
        Topic topic = new Topic(5000);

        Thread serverThread = new Thread(new Server(messageQueue, topic));
        Thread client1Thread = new Thread(new Client("Client 1", messageQueue, topic));
        Thread client2Thread = new Thread(new Client("Client 2", messageQueue, topic));

        serverThread.start();
        client1Thread.start();
        client2Thread.start();
    }
}
