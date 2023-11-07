import message.Message;
import message.MessageQueue;
import topic.Topic;
import topic.TopicMessage;

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

class Server implements Runnable {
    private MessageQueue messageQueue;
    private Topic topic;

    public Server(MessageQueue messageQueue, Topic topic) {
        this.messageQueue = messageQueue;
        this.topic = topic;
    }

    @Override
    public void run() {
        while (true) {
            // The server can perform additional tasks here
        }
    }
}

class Client implements Runnable {
    private String name;
    private MessageQueue messageQueue;
    private Topic topic;

    public Client(String name, MessageQueue messageQueue, Topic topic) {
        this.name = name;
        this.messageQueue = messageQueue;
        this.topic = topic;
    }

    @Override
    public void run() {
        // Simulating sending and receiving messages
        messageQueue.sendMessage(new Message(name, "Hello from " + name, System.currentTimeMillis()));
        System.out.println(name + " received: " + messageQueue.receiveMessage(name).getContent());

        // Simulating topic interactions
        topic.publishMessage(new TopicMessage("type1", "Topic message from " + name, 1000));
        System.out.println(name + " topic messages: " + topic.readMessages("type1"));
    }
}
