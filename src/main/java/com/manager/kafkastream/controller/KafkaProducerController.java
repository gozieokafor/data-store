package com.manager.kafkastream.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.manager.kafkastream.service.KafkaMessageProducer;
import com.manager.kafkastream.service.RocksDBService;


@RestController
@RequestMapping("/api/kafka")
public class KafkaProducerController {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final RocksDBService rocksDBService;

    public KafkaProducerController(KafkaMessageProducer kafkaMessageProducer
    ,RocksDBService rocksDBService) {
        this.kafkaMessageProducer = kafkaMessageProducer;
        this.rocksDBService = rocksDBService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String key, @RequestBody String message) {
        try {
            kafkaMessageProducer.sendMessage(key, message);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message: " + e.getMessage());
        }
    }

    @PostMapping("/sendBatch")
     public ResponseEntity<String> sendBatchMessages(@RequestBody Map<String, String> messages) {
        try {
        messages.forEach(kafkaMessageProducer::sendMessage);
        return ResponseEntity.ok("Batch messages sent successfully");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message: " + e.getMessage());
    }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMessage(@RequestParam String key) {
        boolean kafkaDeleted = kafkaMessageProducer.deleteMessage(key);
        boolean rocksDbDeleted = rocksDBService.deleteFromRocksDB(key);

        if (kafkaDeleted && rocksDbDeleted) {
            return ResponseEntity.ok("Message deleted from Kafka and RocksDB");
        } else if (kafkaDeleted) {
            return ResponseEntity.ok("Message deleted from Kafka but not found in RocksDB");
        } else if (rocksDbDeleted) {
            return ResponseEntity.ok("Message deleted from RocksDB but not found in Kafka");
        } else {
            return ResponseEntity.badRequest().body("Message not found in Kafka or RocksDB");
        }
    }
}
