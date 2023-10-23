# Messaging Server

## Project Specifications

The Messaging Server is designed to provide a multi-threaded messaging platform that includes direct messaging and topic-based subscriptions. Users, referred to as Clients, can send messages to each other via a message queue. Additionally, they can publish or subscribe to topics through which messages can be broadcasted. An Admin thread serves for monitoring and potentially managing the status of the queue and topics.

This system aims to be a low-latency, high-concurrency solution for a multitude of real-world applications such as chat rooms, notification services, or real-time data feeds. With this design, it is essential that messages are processed in a manner that is both fast and reliable, even as the number of clients or topics scales.

Given the necessity for real-time performance and high-concurrency, the underlying data structures for message storage and topic subscriptions must be optimized for quick read and write operations. Care must be taken to ensure that the system does not become a bottleneck when dealing with a large number of simultaneous messages or clients.

## Potential Concurrency Issues

- **Race Conditions**: Multiple threads (clients) could attempt to read or write to the message queue or topic lists simultaneously.
- **Deadlocks**: Poorly managed thread locking could lead to a situation where two or more threads are waiting for each other to release locks.
- **Starvation**: Given that older messages are removed when the queue is full, high throughput could result in some messages being lost before they are read.
- **Inconsistency**: If not properly managed, a thread might read data that is currently being modified by another thread, leading to inconsistent states.

## Proposed Architecture

### Modules
- **MessagingServer**: The main module initiating all components and threads.
- **MessageQueue**: Manages the queue of messages between clients.
- **Topic**: Manages topic-based messaging.

### Classes
- **Server**: Responsible for processing the message queue and topics.
- **Client**: Sends and receives messages, and interacts with topics.
- **Admin**: Monitors the status of the message queue and topics.
- **Message**: Represents a direct message.
- **TopicMessage**: Represents a topic message.

### Threads
- **Server Thread**: Processes the message queue and topics continuously.
- **Client Threads**: Each client runs in its own thread for sending and receiving messages.
- **Admin Thread**: Runs in a separate thread to monitor and possibly manage the server status.

### Interactions
- **Client-Server**: Clients send and receive messages through the Server's message queue and topic mechanisms.
- **Admin-Server**: Admin can monitor or alter the state of the message queue and topics.

### Entry-Point
The `MessagingServer` class serves as the entry point. It initializes the `MessageQueue` and `Topic` and starts the Server, Client, and Admin threads.
