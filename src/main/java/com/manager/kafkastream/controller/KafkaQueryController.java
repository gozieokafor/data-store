package com.manager.kafkastream.controller;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class KafkaQueryController {

    private final RocksDB rocksDB;

    public KafkaQueryController(RocksDB rocksDB) {
        this.rocksDB = rocksDB;
    }

    @GetMapping("/query")
    public ResponseEntity<String> getMessageByKey(@RequestParam String key) {
        try {
            byte[] value = rocksDB.get(key.getBytes(StandardCharsets.UTF_8));
            if (value != null) {
                return ResponseEntity.ok(new String(value, StandardCharsets.UTF_8));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found");
            }
        } catch (RocksDBException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading from RocksDB: " + e.getMessage());
        }
    }

    @GetMapping("/query/batch")
    public ResponseEntity<Map<String, String>> getMessagesByRange(@RequestParam String startKey, @RequestParam String endKey) {
        Map<String, String> result = new HashMap<>();
        try (RocksIterator iterator = rocksDB.newIterator()) {
            iterator.seek(startKey.getBytes(StandardCharsets.UTF_8));
            while (iterator.isValid() && new String(iterator.key(), StandardCharsets.UTF_8).compareTo(endKey) <= 0) {
                result.put(new String(iterator.key(), StandardCharsets.UTF_8), new String(iterator.value(), StandardCharsets.UTF_8));
                iterator.next();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
