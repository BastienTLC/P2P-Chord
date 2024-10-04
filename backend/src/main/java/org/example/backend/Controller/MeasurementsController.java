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
@CrossOrigin(origins = "*")
public class MeasurementsController {

    private ChordController chordController = new ChordController();


    @GetMapping("/runTest")
    public Map<String, Object> runPerformanceTest(
            @RequestParam int nbNodes,
            @RequestParam int nbMessages,
            @RequestParam int dataSize,
            @RequestParam(value = "multiThreading", required = false, defaultValue = "false") boolean multiThreadingEnabled) {

        Map<String, Object> performanceData = new HashMap<>();

        try {
            chordController.runNodes(nbNodes, multiThreadingEnabled);

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
                throw new RuntimeException("Chord network is not ready after waiting " + maxWaitTimeMs );
            }

            List<Message> messages = createMessages(nbMessages, dataSize);

            performanceData.put("storeMetrics", storeMessages(messages, dataSize));
            performanceData.put("retrieveMetrics", retrieveMessages(messages, dataSize));
            performanceData.put("nbNodes", nbNodes);
            performanceData.put("nbMessages", nbMessages);
            performanceData.put("dataSize", dataSize);
            performanceData.put("multiThreadingEnabled", multiThreadingEnabled);
            performanceData.put("executionTimestamp", System.currentTimeMillis());
            performanceData.put("entryNode", chordController.getInitialHost() + ":" + chordController.getInitialPort());

            // Stop the nodes
            for (int i = 0; i < nbNodes; i++) {
                NodeServices nodeServices = new NodeServices(chordController.getInitialHost(), chordController.getInitialPort() + i);
                nodeServices.stopNode();
            }

        } catch (Exception e) {
            e.printStackTrace();
            performanceData.put("error", e.getMessage());
        }

        return performanceData;
    }




    @GetMapping("/runTestScalingMessages")
    public List<Map<String, Object>> runTestScalingMessages(
            @RequestParam int nbNodes,
            @RequestParam int startMessages,
            @RequestParam int increment,
            @RequestParam int maxMessages,
            @RequestParam int dataSize) {

        List<Map<String, Object>> results = new ArrayList<>();

        for (boolean multiThreadingEnabled : new boolean[]{false, true}) {
            for (int nbMessages = startMessages; nbMessages <= maxMessages; nbMessages += increment) {
                Map<String, Object> fullResult = runPerformanceTest(nbNodes, nbMessages, dataSize, multiThreadingEnabled);

                long executionTimestamp = System.currentTimeMillis();
                if (fullResult.containsKey("executionTimestamp")) {
                    executionTimestamp = ((Number) fullResult.get("executionTimestamp")).longValue();
                }

                if (fullResult.containsKey("error")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("nbMessages", nbMessages);
                    result.put("error", fullResult.get("error"));
                    result.put("multiThreadingEnabled", multiThreadingEnabled);
                    result.put("executionTimestamp", executionTimestamp);
                    results.add(result);
                    continue;
                }

                Map<String, Object> storeMetrics = (Map<String, Object>) fullResult.get("storeMetrics");
                Map<String, Object> retrieveMetrics = (Map<String, Object>) fullResult.get("retrieveMetrics");

                long totalStoreTimeMs = ((Number) storeMetrics.get("totalStoreTimeMs")).longValue();
                long totalRetrieveTimeMs = ((Number) retrieveMetrics.get("totalRetrieveTimeMs")).longValue();

                Map<String, Object> result = new HashMap<>();
                result.put("nbMessages", nbMessages);
                result.put("totalStoreTimeMs", totalStoreTimeMs);
                result.put("totalRetrieveTimeMs", totalRetrieveTimeMs);
                result.put("multiThreadingEnabled", multiThreadingEnabled);
                result.put("executionTimestamp", executionTimestamp);

                results.add(result);
            }
        }

        return results;
    }

    @GetMapping("/runTestScalingNodes")
    public List<Map<String, Object>> runTestScalingNodes(
            @RequestParam int startNodes,
            @RequestParam int increment,
            @RequestParam int maxNodes,
            @RequestParam int nbMessages,
            @RequestParam int dataSize) {

        List<Map<String, Object>> results = new ArrayList<>();

        for (boolean multiThreadingEnabled : new boolean[]{false, true}) {
            for (int nbNodes = startNodes; nbNodes <= maxNodes; nbNodes += increment) {
                Map<String, Object> fullResult = runPerformanceTest(nbNodes, nbMessages, dataSize, multiThreadingEnabled);

                long executionTimestamp = System.currentTimeMillis();
                if (fullResult.containsKey("executionTimestamp")) {
                    executionTimestamp = ((Number) fullResult.get("executionTimestamp")).longValue();
                }

                if (fullResult.containsKey("error")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("nbNodes", nbNodes);
                    result.put("error", fullResult.get("error"));
                    result.put("multiThreadingEnabled", multiThreadingEnabled);
                    result.put("executionTimestamp", executionTimestamp);
                    results.add(result);
                    continue;
                }

                Map<String, Object> storeMetrics = (Map<String, Object>) fullResult.get("storeMetrics");
                Map<String, Object> retrieveMetrics = (Map<String, Object>) fullResult.get("retrieveMetrics");

                long totalStoreTimeMs = ((Number) storeMetrics.get("totalStoreTimeMs")).longValue();
                long totalRetrieveTimeMs = ((Number) retrieveMetrics.get("totalRetrieveTimeMs")).longValue();

                Map<String, Object> result = new HashMap<>();
                result.put("nbNodes", nbNodes);
                result.put("totalStoreTimeMs", totalStoreTimeMs);
                result.put("totalRetrieveTimeMs", totalRetrieveTimeMs);
                result.put("multiThreadingEnabled", multiThreadingEnabled);
                result.put("executionTimestamp", executionTimestamp);

                results.add(result);
            }
        }

        return results;
    }

    @GetMapping("/runTestScalingDataSize")
    public List<Map<String, Object>> runTestScalingDataSize(
            @RequestParam int nbNodes,
            @RequestParam int nbMessages,
            @RequestParam int startDataSize,
            @RequestParam int increment,
            @RequestParam int maxDataSize) {

        List<Map<String, Object>> results = new ArrayList<>();

        for (boolean multiThreadingEnabled : new boolean[]{false, true}) {
            for (int dataSize = startDataSize; dataSize <= maxDataSize; dataSize += increment) {
                Map<String, Object> fullResult = runPerformanceTest(nbNodes, nbMessages, dataSize, multiThreadingEnabled);

                long executionTimestamp = System.currentTimeMillis();
                if (fullResult.containsKey("executionTimestamp")) {
                    executionTimestamp = ((Number) fullResult.get("executionTimestamp")).longValue();
                }

                if (fullResult.containsKey("error")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("dataSize", dataSize);
                    result.put("error", fullResult.get("error"));
                    result.put("multiThreadingEnabled", multiThreadingEnabled);
                    result.put("executionTimestamp", executionTimestamp);
                    results.add(result);
                    continue;
                }

                Map<String, Object> storeMetrics = (Map<String, Object>) fullResult.get("storeMetrics");
                Map<String, Object> retrieveMetrics = (Map<String, Object>) fullResult.get("retrieveMetrics");

                long totalStoreTimeMs = ((Number) storeMetrics.get("totalStoreTimeMs")).longValue();
                long totalRetrieveTimeMs = ((Number) retrieveMetrics.get("totalRetrieveTimeMs")).longValue();

                Map<String, Object> result = new HashMap<>();
                result.put("dataSize", dataSize);
                result.put("totalStoreTimeMs", totalStoreTimeMs);
                result.put("totalRetrieveTimeMs", totalRetrieveTimeMs);
                result.put("multiThreadingEnabled", multiThreadingEnabled);
                result.put("executionTimestamp", executionTimestamp);

                results.add(result);
            }
        }

        return results;
    }

    @GetMapping("/runAllTests")
    public Map<String, List<Map<String, Object>>> runAllTests(
            @RequestParam int startNodes,
            @RequestParam int incrementNodes,
            @RequestParam int maxNodes,
            @RequestParam int startMessages,
            @RequestParam int incrementMessages,
            @RequestParam int maxMessages,
            @RequestParam int startDataSize,
            @RequestParam int incrementDataSize,
            @RequestParam int maxDataSize) {

        int normalNbNodes = 5;
        int normalNbMessages = 100;
        int normalDataSize = 1024;

        Map<String, List<Map<String, Object>>> allResults = new HashMap<>();

        // Run test scaling nodes
        List<Map<String, Object>> nodesResults = runTestScalingNodes(
                startNodes, incrementNodes, maxNodes, normalNbMessages, normalDataSize);
        allResults.put("nodesResults", nodesResults);

        // Run test scaling messages
        List<Map<String, Object>> messagesResults = runTestScalingMessages(
                normalNbNodes, startMessages, incrementMessages, maxMessages, normalDataSize);
        allResults.put("messagesResults", messagesResults);

        // Run test scaling data size
        List<Map<String, Object>> dataSizeResults = runTestScalingDataSize(
                normalNbNodes, normalNbMessages, startDataSize, incrementDataSize, maxDataSize);
        allResults.put("dataSizeResults", dataSizeResults);

        //save results to file json

        return allResults;
    }





    private Map<String, Object> storeMessages(List<Message> messages, int dataSize) {
        List<Long> storeTimes = new ArrayList<>();
        long totalStoreTime = 0L;
        int successfulStores = 0;
        NodeServices nodeServices = new NodeServices(chordController.getInitialHost(), chordController.getInitialPort());

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
        double bandWidth = (dataSize * successfulStores * 8) / totalSeconds; // In bits per second
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
        NodeServices nodeServices = new NodeServices(chordController.getInitialHost(), chordController.getInitialPort());

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

        double totalSeconds = totalRetrieveTime / 1000.0;
        double bandWidth = (dataSize * successfulRetrieves) / totalSeconds;
        double throughput = successfulRetrieves / totalSeconds;

        Map<String, Object> retrieveMetrics = new HashMap<>();
        retrieveMetrics.put("totalRetrieveTimeMs", totalRetrieveTime);
        retrieveMetrics.put("bandWidthBytesPerSec", bandWidth);
        retrieveMetrics.put("throughputMessagesPerSec", throughput);
        retrieveMetrics.put("successfulRetrieves", successfulRetrieves);
        retrieveMetrics.put("failedRetrieves", messages.size() - successfulRetrieves);

        return retrieveMetrics;
    }
}
