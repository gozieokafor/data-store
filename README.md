# Data-Store Repository

Welcome to the Data-Store repository! This project is designed to provide a robust, scalable, and efficient data storage solution using Kafka and RocksDB. Below, you'll find all the necessary information to get started, along with details about the architecture, trade-offs, and optimizations.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Docker**: Required to build and deploy the containers.
- **Java 17**: Required to run the Spring Boot application.

## Getting Started

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>

2. **Navigate to the compose.yaml Location**:
   ```bash
   cd path/to/compose.yaml

3. **Build and Deploy the Containers**:
   ```bash
   docker-compose build
   docker-compose up

4. **This will create and start two containers**:
   **Kafka**: A distributed event streaming platform.
   **RocksDB**: A high-performance embedded database for key-value data.

5. **Wait for Kafka to Initialize**:
   After the containers are up and running, give Kafka a brief moment (about a minute) to completely set up.

## Interact with the API
   Once everything is set up, you can use curl or Postman to interact with the API. The base URL for all endpoints is api/kafka.


  1. **Send a Message**:
     ```bash
     curl -X POST http://localhost:8080/api/kafka/send -d '{"key": "yourKey", "value": "yourValue"}'
    
  2. **Send a Batch of Messages**:
     ```bash
     curl -X POST http://localhost:8080/api/kafka/sendBatch -d '[{"key": "key1", "value": "value1"}, {"key": "key2", "value": "value2"}]'
     
  3. **Query by Key**:
     ```bash
     curl -X GET http://localhost:8080/api/kafka/query?key=yourKey
     
  4. **Query Batch by Key Range**:
     ```bash
     curl -X GET http://localhost:8080/api/kafka/queryBatch?startKey=startKey&endKey=endKey

  5. **Delete a Message by Key**:
     ```bash
     curl -X DELETE http://localhost:8080/api/kafka/delete?key=yourKey


## Architecture Overview
This project is a **Java Spring Boot** application that produces and consumes messages to and from a **Kafka** instance. 
The consumed data is then persisted to a **RocksDB** instance for durable storage.


##  Pros and Cons

**Pros**:
**Low Latency**: Optimized for low-latency data access.
**High Throughput**: Capable of handling a high volume of messages efficiently.
**Durability**: Data is persisted to RocksDB, ensuring durability even in the event of a system failure.

**Cons**:
**Complexity**: The addition of RocksDB adds complexity to the architecture.
**Potential Failure Point**: The **query API** node is an additional component that could fail, requiring monitoring and maintenance.

## Optimizations
To ensure low latency, high throughput, and durability, the following optimizations have been implemented:

**Efficient Kafka Producers and Consumers**: Optimized for high throughput and low latency.
**RocksDB Tuning**: Configured for high write and read performance.
**Batch Processing**: Supports batch operations to reduce overhead and improve throughput.

## Conclusion
This repository provides a scalable and efficient data storage solution leveraging Kafka and RocksDB. 
By following the steps above, you can quickly get the system up and running. Enjoy exploring the capabilities of this architecture!

For any issues or further assistance, please refer to the documentation or reach out to the maintainers.

Happy coding! 🚀
