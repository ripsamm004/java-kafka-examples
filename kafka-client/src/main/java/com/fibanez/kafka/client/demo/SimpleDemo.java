package com.fibanez.kafka.client.demo;

import com.fibanez.kafka.client.consumer.SimpleConsumer;
import com.fibanez.kafka.client.producer.SimpleProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by fibanez on 10/6/17.
 */
public class SimpleDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDemo.class);

    private  ExecutorService executor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {

        try {
            SimpleDemo demo = new SimpleDemo();
            demo.start("simpleDemo", true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start(String topic, boolean asyn) throws InterruptedException {

        // ctrl-c kill
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { shutdown(); }));

        LOGGER.info("Starting simple demo");

        SimpleConsumer consumer = new SimpleConsumer(topic);
        SimpleProducer producer = new SimpleProducer(topic,asyn);

        executor.submit(consumer);
        executor.submit(producer);

        TimeUnit.SECONDS.sleep(5);

        consumer.shutdown();
//        producer.shutdown();
        shutdown();

        LOGGER.info("Finished simple demo");

        System.exit(0);
    }

    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            LOGGER.error("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                LOGGER.error("cancel non-finished tasks");
                executor.shutdownNow();
            }
            LOGGER.info("shutdown finished");
        }

    }

}
