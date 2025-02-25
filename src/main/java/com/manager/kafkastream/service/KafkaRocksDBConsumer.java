package com.manager.kafkastream.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaRocksDBConsumer {

    private final RocksDB rocksDB;

    //Inject RocksDB from KafkaRocksDBConfig
    public KafkaRocksDBConsumer(RocksDB rocksDB) {
        this.rocksDB = rocksDB;
    }

    //Kafka Consumer will be automatically managed by Spring Boot
    @KafkaListener(topics = "input-topic", groupId = "kafka-group")
    public void consume(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();

        try {
            rocksDB.put(key.getBytes(), value.getBytes()); //Store message in RocksDB
            System.out.println("Stored in RocksDB: Key=" + key + ", Value=" + value);
        } catch (RocksDBException e) {
            System.err.println("Error storing in RocksDB: " + e.getMessage());
        }
    }
}
