package com.manager.kafkastream.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;


@Service
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> template;
    private final String topic = "input-topic";

    private static final Logger logger = Logger.getLogger(KafkaMessageProducer.class.getName());

    public KafkaMessageProducer(KafkaTemplate<String, String> template) {
        this.template = template;
    }
    
    public void sendMessage(String key, String message) {
        template.send(new ProducerRecord<>(topic, key, message))
                .whenComplete((sendResult, exception) -> {
                    if (exception != null) {
                        logger.log(Level.SEVERE, "Failed to send message with key {0}: {1}",
                                new Object[]{key, exception.getMessage()});
                    } else {
                        RecordMetadata metadata = sendResult.getRecordMetadata();
                        logger.log(Level.INFO, "Message sent successfully to topic {0}, partition {1}, offset {2}",
                                new Object[]{metadata.topic(), metadata.partition(), metadata.offset()});
                    }
                });
    }

    public void sendBatchMessages(Map<String, String> messages) {
        try {
            List<CompletableFuture<RecordMetadata>> futures = messages.entrySet().stream()
                    .map(entry -> CompletableFuture.supplyAsync(()
                    -> sendAsync(entry.getKey(), entry.getValue())
            ))
                    .toList();

            // Wait for all messages to be sent before proceeding
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            // Force flush to ensure messages are actually sent
            template.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending batch messages to Kafka: {0}", e.getMessage());
        }
    }

    private RecordMetadata sendAsync(String key, String message) {
        try {
            SendResult<String, String> sendResult = template.send(new ProducerRecord<>(topic, key, message))
                                                            .get(1, TimeUnit.SECONDS);
            return sendResult.getRecordMetadata();  // Extract RecordMetadata properly
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.log(Level.SEVERE, "Failed to send async message with key {0}: {1}", 
                       new Object[]{key, e.getMessage()});
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        return null;
    }
    
    public boolean deleteMessage(String key) {
        try {
            // Kafka does not allow direct deletion of messages.
            // Instead, we produce a null value for the key (tombstone message)
            template.send(new ProducerRecord<>(topic, key, null));

        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            logger.log(Level.SEVERE, "Error sending message to Kafka: {0}", e.getMessage());
            return false;
        }
        return true;
    }
}
