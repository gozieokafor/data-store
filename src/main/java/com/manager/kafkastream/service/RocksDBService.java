package com.manager.kafkastream.service;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.stereotype.Service;

@Service
public class RocksDBService {

    private final RocksDB rocksDB;

    public RocksDBService(RocksDB rocksDB) {
        this.rocksDB = rocksDB;
    }

    public boolean deleteFromRocksDB(String key) {
        try {
            rocksDB.delete(key.getBytes());
            return true;
        } catch (RocksDBException e) {
            System.err.println("Error storing in RocksDB: " + e.getMessage());
            return false;
        }
    }
}
