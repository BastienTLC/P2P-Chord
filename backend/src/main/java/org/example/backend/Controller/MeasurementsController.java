package org.example.backend.Controller;

import org.example.backend.Entity.Message;
import org.example.backend.Entity.Node;
import org.example.backend.Services.NodeServices;
import org.example.backend.Utils.Hash;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.example.backend.Utils.Message.createMessages;

@RestController
@RequestMapping("/measurements")
public class MeasurementsController {

    private String initialHost = "localhost";
    private int initialPort = 8000;
    private ChordController chordController = new ChordController();

    @PostMapping("/initialNode/{host}/{port}")
    public boolean setInitialNode(@PathVariable String host, @PathVariable int port) {
        NodeServices nodeServices = new NodeServices(host, port);
        Node initialNode = nodeServices.getChordNodeInfo();
        if (initialNode != null) {
            initialHost = host;
            initialPort = port;
            return true;
        } else {
            System.err.println("Initial node at " + host + ":" + port + " is not available.");
        }
        return false;
    }

    @PostMapping("/generateAndStoreMessages")
    public boolean generateAndStoreMessages(
            @RequestParam int nbMessages,
            @RequestParam int dataSize,
            @RequestParam int nbNodes) {

        try {
            chordController.runNodes(nbNodes);

            // Wait until the nodes are ready, with a maximum timeout
            long maxWaitTimeMs = 30000; // 30 seconds
            long waitedTimeMs = 0;
            long sleepIntervalMs = 2000;
            boolean isReady = false;

            while (waitedTimeMs < maxWaitTimeMs) {
                if (chordController.chordIsReady(nbNodes)) {
                    isReady = true;
                    break;
                }
                try {
                    Thread.sleep(sleepIntervalMs);
                    waitedTimeMs += sleepIntervalMs;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted", e);
                }
            }

            if (!isReady) {
                throw new RuntimeException("Chord network is not ready after waiting " + maxWaitTimeMs + " ms");
            }

            List<Message> messages = createMessages(nbMessages, dataSize);

            NodeServices nodeServices = new NodeServices(initialHost, initialPort);
            for (Message message : messages) {
                String key = Hash.hashKey(message.getAuthor() + ":" + message.getTimestamp());
                message.setId(key);
                nodeServices.storeMessageInChord(key, message);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/runTest")
    public Map<String, Object> runPerformanceTest(
            @RequestParam int nbNodes,
            @RequestParam int nbMessages,
            @RequestParam int dataSize) {

        Map<String, Object> performanceData = new HashMap<>();

        try {
            chordController.runNodes(nbNodes);

            // Wait until the nodes are ready, with a maximum timeout
            long maxWaitTimeMs = 30000; // 30 seconds
            long waitedTimeMs = 0;
            long sleepIntervalMs = 2000;
            boolean isReady = false;

            while (waitedTimeMs < maxWaitTimeMs) {
                if (chordController.chordIsReady(nbNodes)) {
                    isReady = true;
                    break;
                }
                try {
                    Thread.sleep(sleepIntervalMs);
                    waitedTimeMs += sleepIntervalMs;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted", e);
                }
            }

            if (!isReady) {
                throw new RuntimeException("Chord network is not ready after waiting " + maxWaitTimeMs + " ms");
            }

            List<Message> messages = createMessages(nbMessages, dataSize);

            performanceData.put("storeMetrics", storeMessages(messages, dataSize));
            performanceData.put("retrieveMetrics", retrieveMessages(messages, dataSize));
            performanceData.put("nbNodes", nbNodes);
            performanceData.put("nbMessages", nbMessages);
            performanceData.put("dataSize", dataSize);

            // Stop the nodes
            for (int i = 0; i < nbNodes; i++) {
                NodeServices nodeServices = new NodeServices(initialHost, initialPort + i);
                nodeServices.stopNode();
            }

        } catch (Exception e) {
            e.printStackTrace();
            performanceData.put("error", e.getMessage());
        }

        return performanceData;
    }

    private Map<String, Object> storeMessages(List<Message> messages, int dataSize) {
        List<Long> storeTimes = new ArrayList<>();
        long totalStoreTime = 0L;
        int successfulStores = 0;
        NodeServices nodeServices = new NodeServices(initialHost, initialPort);

        for (Message message : messages) {
            String key = Hash.hashKey(message.getAuthor() + ":" + message.getTimestamp());
            message.setId(key);

            long startTime = System.nanoTime();
            try {
                nodeServices.storeMessageInChord(key, message);
                long endTime = System.nanoTime();
                long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                storeTimes.add(duration);
                totalStoreTime += duration;
                successfulStores++;
            } catch (Exception e) {
                e.printStackTrace();
                // Continue to next message
            }
        }

        double totalSeconds = totalStoreTime / 1000.0; // Convert to seconds
        double bandWidth = (dataSize * successfulStores * 8) / totalSeconds; // In bits per second (bps)
        double throughput = successfulStores / totalSeconds; // In messages per second

        Map<String, Object> storeMetrics = new HashMap<>();
        storeMetrics.put("totalStoreTimeMs", totalStoreTime);
        storeMetrics.put("bandWidthBitsPerSec", bandWidth);
        storeMetrics.put("throughputMessagesPerSec", throughput);
        storeMetrics.put("successfulStores", successfulStores);
        storeMetrics.put("failedStores", messages.size() - successfulStores);

        return storeMetrics;
    }

    private Map<String, Object> retrieveMessages(List<Message> messages, int dataSize) {
        List<Long> retrieveTimes = new ArrayList<>();
        long totalRetrieveTime = 0L;
        int successfulRetrieves = 0;
        NodeServices nodeServices = new NodeServices(initialHost, initialPort);

        for (Message message : messages) {
            String key = message.getId();

            long startTime = System.nanoTime();
            try {
                nodeServices.retrieveMessageFromChord(key);
                long endTime = System.nanoTime();
                long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                retrieveTimes.add(duration);
                totalRetrieveTime += duration;
                successfulRetrieves++;
            } catch (Exception e) {
                e.printStackTrace();
                // Continue to next message
            }
        }

        double totalSeconds = totalRetrieveTime / 1000.0; // Convert to seconds
        double bandWidth = (dataSize * successfulRetrieves * 8) / totalSeconds; // In bits per second (bps)
        double throughput = successfulRetrieves / totalSeconds; // In messages per second

        Map<String, Object> retrieveMetrics = new HashMap<>();
        retrieveMetrics.put("totalRetrieveTimeMs", totalRetrieveTime);
        retrieveMetrics.put("bandWidthBitsPerSec", bandWidth);
        retrieveMetrics.put("throughputMessagesPerSec", throughput);
        retrieveMetrics.put("successfulRetrieves", successfulRetrieves);
        retrieveMetrics.put("failedRetrieves", messages.size() - successfulRetrieves);

        return retrieveMetrics;
    }
}
