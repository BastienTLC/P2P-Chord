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

    private String initialHost = "localhost";  // Initial node IP
    private int initialPort = 8000;            // Initial node port


    @PostMapping("/store/{host}/{port}")
    public boolean storeMessageInChord(@PathVariable String host, @PathVariable int port,@RequestBody Message message) {
        NodeServices nodeServices = new NodeServices(host, port);
        String key = Hash.hashKey(message.getAuthor() +":"+ message.getTimestamp());
        message.setId(key);
        return nodeServices.storeMessageInChord(key, message);
    }

    @PostMapping("/store")
    public boolean storeMessageInChord(@RequestBody Message message) {
        NodeServices nodeServices = new NodeServices(initialHost, initialPort);
        String key = Hash.hashKey(message.getAuthor() +":"+ message.getTimestamp());
        message.setId(key);
        return nodeServices.storeMessageInChord(Hash.hashKey(message.getAuthor() +":"+ message.getTimestamp()), message);
    }


    @GetMapping("/retrieve/{key}")
    public Message retrieveMessageFromChord(@PathVariable String key) {
        NodeServices nodeServices = new NodeServices(initialHost, initialPort);
        return nodeServices.retrieveMessageFromChord(Hash.hashKey(key));
    }

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

    @PatchMapping("/run/{nbNodes}")
    public boolean runNodes(@PathVariable int nbNodes) {
        int lastPort = getNetworkRing(100).values().stream()
                .mapToInt(Node::getPort)
                .max()
                .orElse(8000);

        for (int i = lastPort + 1; i <= lastPort + nbNodes; i++) {
            NodeServices nodeServices = new NodeServices("localhost", i);
            nodeServices.runNode("localhost", i, initialHost, initialPort);
        }
        return true;
    }

    // Endpoint to get the network ring data with limited recursion depth
    @GetMapping("/ring/{depth}")
    public Map<String, Node> getNetworkRing(@PathVariable int depth) {
        // Start with the initial node
        NodeServices nodeServices = new NodeServices(initialHost, initialPort);
        Node initialNode = nodeServices.getChordNodeInfo();

        // HashMap to store nodeId => Node mappings
        Map<String, Node> nodeMap = new HashMap<>();

        // Recursively traverse the network and populate the HashMap
        traverseNetworkRing(initialNode, depth, nodeMap);

        return nodeMap; // Return the HashMap to the client
    }

    // Recursive method to traverse the network and fill the node map
    private void traverseNetworkRing(Node currentNode, int depth, Map<String, Node> nodeMap) {
        if (depth <= 0 || currentNode == null) {
            return;  // Stop recursion when depth limit is reached
        }

        // Add the current node to the map
        nodeMap.put(currentNode.getId(), currentNode);

        // Get the successor of the current node
        NodeHeader successorHeader = currentNode.getPredecessor();
        if (successorHeader != null && !nodeMap.containsKey(successorHeader.getId())) {
            // Get the successor node's info
            NodeServices successorServices = new NodeServices(successorHeader.getIp(), successorHeader.getPort());
            Node successorNode = successorServices.getChordNodeInfo();

            // Recursively traverse the successor node
            traverseNetworkRing(successorNode, depth - 1, nodeMap);
        }
    }

    public boolean chordIsReady(int numberOfNodes) {
        Map<String, Node> networkRing = getNetworkRing(100);
        return networkRing.size() >= numberOfNodes;
    }

}
