# Messaging Server

## Project Specifications

The Messaging Server is a sophisticated, multi-threaded messaging system designed to facilitate both direct and
topic-based messaging in a high-concurrency environment. It offers a platform where clients can exchange messages,
subscribe to various topics, and receive real-time updates, making it suitable for applications like chat rooms,
notification systems, and real-time data streaming. The system's architecture emphasizes efficiency and reliability,
ensuring swift message processing and robust handling of numerous simultaneous interactions.

A key component of this system is the `ViralService`, a dedicated process for analyzing message trends, particularly by
tracking hashtag usage in broadcast and topic messages. This service highlights the system's capability for real-time
data analysis and trend monitoring.

## Concurrency Issues and Resolutions

- **Race Conditions**: Addressed through the use of thread-safe collections like `ConcurrentHashMap` and synchronization
  mechanisms in classes like `MessageQueue` and `TopicOrchestrator`, ensuring safe concurrent read/write operations.
- **Deadlocks**: Avoided by designing a non-blocking message processing mechanism in the `Server` class and implementing
  asynchronous message handling using the `RabbitMQManager`.
- **Starvation**: Prevented by employing a fair queuing system in `MessageQueue` and ensuring equitable message
  processing.
- **Inconsistency**: Mitigated by utilizing atomic operations for message handling and ensuring immutable states of
  messages once created.
- **Thread Interruption Handling**: Properly managing thread interruptions in classes like `Client` and `ViralService`
  to handle unexpected disruptions in thread execution.
- **Graceful Shutdown Management**: The system employs `addShutdownHook` methods in both `MessagingServer`
  and `ViralService` for proper shutdown and resource cleanup. This ensures that all threads and processes are
  terminated in an orderly manner, preventing resource leakage and ensuring that the system state is consistent and
  stable during shutdown sequences.

The system's architecture and choice of data structures are specifically tailored to manage these challenges, ensuring
that the messaging process remains consistent, reliable, and efficient.

## Proposed Architecture

### Modules

- **MessagingServer**: Initializes and orchestrates the server, clients, and the ViralService.
- **MessageQueue**: Manages a thread-safe queue of direct messages.
- **TopicOrchestrator**: Handles topic-based message storage and delivery.

### Classes and Processes

- **Server**: Processes and routes direct and topic messages to clients.
- **Client**: Handles sending and receiving messages, and interacts with topics.
- **ViralService**: A separate service process for analyzing and displaying trending hashtags in messages.
- **Message**: Represents direct messages between clients.
- **TopicMessage**: Represents messages published to topics.
- **RabbitMQManager**: Manages interactions with RabbitMQ for message queuing and topic management.

### Threads

- **Server Thread**: Manages message routing and topic message processing.
- **Client Threads**: Each operates in its own thread, sending and receiving messages.
- **ViralService Thread**: Runs independently, in a separate process, analyzing message trends.

### Interactions

- **Client-Server**: Clients interact with the server for message sending/receiving and topic subscriptions.
- **ViralService-Server**: The ViralService consumes messages from RabbitMQ to analyze trends.

### Entry-Point

The main entry point of the system is the `MessagingServer` class. It initiates the `Server` and `Client` threads. The
server handles the core functionalities of message queuing and topic management, and allows the clients to communicate
to each other, or to publish messages to specific topics.

The other entry point is `ViralService`, which independently processes message data taken from RabbitMQ, for trend
analysis.

### External Interactions

- **RabbitMQ Integration**: Facilitates distributed message processing and ensures scalability and resilience of the
  system.
