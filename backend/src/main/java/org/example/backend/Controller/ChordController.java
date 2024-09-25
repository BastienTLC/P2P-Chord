package org.example.backend.Controller;

import org.example.backend.Entity.Node;
import org.example.backend.Entity.NodeHeader;
import org.example.backend.Services.NodeServices;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/network")
public class ChordController {

    private final String initialHost = "localhost";  // Initial node IP
    private final int initialPort = 8000;            // Initial node port

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
}
