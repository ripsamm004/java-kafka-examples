package com.fibanez.kafka.avro.consumer;

import com.fibanez.kafka.avro.model.MessageAvro;
import com.fibanez.kafka.client.model.KafkaMessage;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.Properties;

/**
 * Created by fibanez on 10/6/17.
 */
public class AvroConsumer implements Runnable  {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroConsumer.class);

    private final KafkaConsumer<Integer, MessageAvro> consumer;
    private final String topic;
    private boolean shutdown;

    public AvroConsumer(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-consumer-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());

        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", true);

        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
    }


    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(this.topic));

        LOGGER.info("Consumer is waiting on topics {}",  this.topic);

        try {

            KafkaMessage message;
            ConsumerRecords<Integer, MessageAvro> records;

            while (!shutdown) {

                records = consumer.poll(1000);

                if (records.count() > 0) {
                    LOGGER.info("Number of Records = " + records.count());
                }

                for (ConsumerRecord<Integer, MessageAvro> record : records) {
                    LOGGER.info(
                            "Received message: topic = {}, partition = {}, offset = {}, timestamp = {} \n Received message({},{}) ",
                            record.topic(), record.partition(), record.offset(), new Date(record.timestamp()), record.key(), record.value()
                    );
                }
            }

        } catch (WakeupException e) {
            LOGGER.info("WakeupException handled");
            // ignore, we're closing
        } catch (Throwable t) {
            LOGGER.error("Unexpected error", t);
        } finally {
            consumer.close();
        }

    }

    public void shutdown() {
        shutdown = true;
    }
}