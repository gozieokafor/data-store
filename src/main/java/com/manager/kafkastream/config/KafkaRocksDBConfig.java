package com.manager.kafkastream.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka  //Enables Kafka support in Spring Boot
public class KafkaRocksDBConfig {

    private static final String ROCKS_DB_PATH = "/tmp/rocksdb";

    //Kafka Producer Configuration
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        //props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.19.0.4:9092,172.19.0.2:9094,172.19.0.3:9096");
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        /*if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = "kafka1:9092,kafka2:9094,kafka3:9096"; // Default value
        }*/
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Optimized Producer Settings
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure all replicas acknowledge
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64 KB batch size
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms to form larger batches
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); // Use LZ4 compression
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);   

        props.put(ProducerConfig.RETRIES_CONFIG, 10); // Retry up to 10 times
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // Wait 100ms before retrying
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer memory
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "optimized-producer");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    //Kafka Consumer Configuration
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        //props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.19.0.4:9092,172.19.0.2:9094,172.19.0.3:9096");
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        /*if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = "kafka1:9092,kafka2:9094,kafka3:9096"; // Default value
        }*/
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //Start consuming from beginning if no offset

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    //RocksDB Configuration
    @SuppressWarnings("resource") // Suppress false positive warning
    @Bean
    public RocksDB rocksDB() throws RocksDBException {
        RocksDB.loadLibrary();
        try (Options options = new Options().setCreateIfMissing(true)) {
            return RocksDB.open(options, ROCKS_DB_PATH);
        }
    }

    //Kafka Topic Creation (Optional)
    @Bean
    public NewTopic myTopic() {
        return new NewTopic("input-topic", 1, (short) 3); // 1 partitions, replication factor of 3
    }
}
