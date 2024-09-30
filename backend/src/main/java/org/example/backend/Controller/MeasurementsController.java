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

    // Fonction pour définir le nœud initial si nécessaire
    @PostMapping("/initialNode/{host}/{port}")
    public boolean setInitialNode(@PathVariable String host, @PathVariable int port) {
        NodeServices nodeServices = new NodeServices(host, port);
        Node initialNode = nodeServices.getChordNodeInfo();
        if (initialNode != null) {
            initialHost = host;
            initialPort = port;
            return true;
        }
        return false;
    }

    // Génération et stockage des messages
    @PostMapping("/generateAndStoreMessages")
    public boolean generateAndStoreMessages(
            @RequestParam int quantity,
            @RequestParam int dataSize,
            @RequestParam int nbNodes) {

        // Lancer les nœuds
        chordController.runNodes(nbNodes);

        // Générer les messages
        List<Message> messages = createMessages(quantity, dataSize);

        // Stocker les messages
        NodeServices nodeServices = new NodeServices(initialHost, initialPort);
        for (Message message : messages) {
            String key = Hash.hashKey(message.getAuthor() + ":" + message.getTimestamp());
            message.setId(key);
            nodeServices.storeMessageInChord(key, message);
        }

        return true;
    }

    // Fonction pour exécuter les tests de performance
    @GetMapping("/runTest")
    public Map<String, Object> runPerformanceTest(
            @RequestParam int nbNodes,
            @RequestParam int nbMessages,
            @RequestParam int dataSize) {

        // Lancer les nœuds
        chordController.runNodes(nbNodes);

        while(!chordController.chordIsReady(nbNodes + 1)) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Générer les messages
        List<Message> messages = createMessages(nbMessages, dataSize);

        // Mesures de performance
        Map<String, Object> performanceData = new HashMap<>();
        List<Long> storeTimes = new ArrayList<>();
        List<Long> retrieveTimes = new ArrayList<>();

        NodeServices nodeServices = new NodeServices(initialHost, initialPort);

        // Stocker les messages et mesurer le temps
        for (Message message : messages) {
            String key = Hash.hashKey(message.getAuthor() + ":" + message.getTimestamp());
            message.setId(key);
            long startTime = System.nanoTime();
            nodeServices.storeMessageInChord(key, message);
            long endTime = System.nanoTime();
            storeTimes.add(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
        }

        // Récupérer les messages et mesurer le temps
        for (Message message : messages) {
            String key = message.getId();
            long startTime = System.nanoTime();
            nodeServices.retrieveMessageFromChord(key);
            long endTime = System.nanoTime();
            retrieveTimes.add(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
        }

        performanceData.put("nbNodes", nbNodes);
        performanceData.put("nbMessages", nbMessages);
        performanceData.put("dataSize", dataSize);
        performanceData.put("storeTimes", storeTimes);
        performanceData.put("retrieveTimes", retrieveTimes);

        for (int i = 0; i < nbNodes; i++) {
            nodeServices = new NodeServices(initialHost, initialPort + i);
            nodeServices.stopNode();
        }
        return performanceData;
    }

    @GetMapping("/runTestScalingNodes")
    public List<Map<String, Object>> runTestScalingNodes(
            @RequestParam int startNodes,
            @RequestParam int endNodes,
            @RequestParam int stepNodes,
            @RequestParam int nbMessages,
            @RequestParam int dataSize) {

        List<Map<String, Object>> results = new ArrayList<>();

        for (int nbNodes = startNodes; nbNodes <= endNodes; nbNodes += stepNodes) {
            Map<String, Object> result = runPerformanceTest(nbNodes, nbMessages, dataSize);
            results.add(result);
        }

        return results;
    }
}
