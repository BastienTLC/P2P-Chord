package org.example.backend.Controller;

import org.example.backend.Entity.Message;
import org.example.backend.Entity.Node;
import org.example.backend.Entity.NodeHeader;
import org.example.backend.Services.NodeServices;
import org.example.backend.Utils.Hash;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/network")
@CrossOrigin(origins = "*")
public class ChordController {

    private String initialHost = "localhost";
    private int initialPort = 8000;

    public String getInitialHost() {
        return initialHost;
    }

    public int getInitialPort() {
        return initialPort;
    }

    @PostMapping("/store/{host}/{port}")
    public boolean storeMessageInChord(@PathVariable String host, @PathVariable int port, @RequestBody Message message) {
        try {
            NodeServices nodeServices = new NodeServices(host, port);
            String key = Hash.hashKey(message.getAuthor() + ":" + message.getTimestamp());
            message.setId(key);
            return nodeServices.storeMessageInChord(key, message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/store")
    public boolean storeMessageInChord(@RequestBody Message message) {
        try {
            NodeServices nodeServices = new NodeServices(initialHost, initialPort);
            String key = Hash.hashKey(message.getAuthor() + ":" + message.getTimestamp());
            message.setId(key);
            return nodeServices.storeMessageInChord(key, message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/retrieve/{key}")
    public Message retrieveMessageFromChord(@PathVariable String key) {
        try {
            NodeServices nodeServices = new NodeServices(initialHost, initialPort);
            return nodeServices.retrieveMessageFromChord(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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

    @PatchMapping("/run/{nbNodes}")
    public boolean runNodes(@PathVariable int nbNodes, @RequestParam(value = "multiThreading", required = false, defaultValue = "false") boolean multiThreadingEnabled) {
        int lastPort = 8000;
        try {
            lastPort = getNetworkRing(100).values().stream()
                    .mapToInt(Node::getPort)
                    .max()
                    .orElse(8000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean allNodesStarted = true;
        for (int i = lastPort; i <= lastPort + nbNodes; i++) {
            NodeServices nodeServices = new NodeServices("localhost", i);
            try {
                if (i == 8000) {
                    // Bootstrap node
                    nodeServices.runNode("localhost", i, multiThreadingEnabled);
                } else {
                    nodeServices.runNode("localhost", i, initialHost, initialPort, multiThreadingEnabled);
                }
            } catch (Exception e) {
                e.printStackTrace();
                allNodesStarted = false;
            }
        }
        return allNodesStarted;
    }

    @GetMapping("/ring/{depth}")
    public Map<String, Node> getNetworkRing(@PathVariable int depth) {
        Map<String, Node> nodeMap = new HashMap<>();
        try {
            NodeServices nodeServices = new NodeServices(initialHost, initialPort);
            Node initialNode = nodeServices.getChordNodeInfo();

            if (initialNode != null) {
                traverseNetworkRing(initialNode, depth, nodeMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Return empty nodeMap
        }
        return nodeMap;
    }

    private void traverseNetworkRing(Node currentNode, int depth, Map<String, Node> nodeMap) {
        if (depth <= 0 || currentNode == null) {
            return;
        }

        nodeMap.put(currentNode.getId(), currentNode);

        NodeHeader successorHeader = currentNode.getSuccessor(); // Corrected from getPredecessor()
        if (successorHeader != null && !nodeMap.containsKey(successorHeader.getId())) {
            try {
                NodeServices successorServices = new NodeServices(successorHeader.getIp(), successorHeader.getPort());
                Node successorNode = successorServices.getChordNodeInfo();

                traverseNetworkRing(successorNode, depth - 1, nodeMap);
            } catch (Exception e) {
                e.printStackTrace();
                // Stop traversing further
            }
        }
    }

    public boolean chordIsReady(int numberOfNodes) {
        try {
            Map<String, Node> networkRing = getNetworkRing(100);
            return networkRing.size() >= numberOfNodes;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
