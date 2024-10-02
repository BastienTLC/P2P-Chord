package org.example.base;

import org.example.types.Message;
import org.example.types.NodeHeader;
import org.example.utils.Wrapper;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ChordNode {
    private final String ip;
    private final int port;
    private final String nodeId; // Unique node ID based on the hash of IP and port
    private NodeHeader successor; // Successor of this node
    private NodeHeader predecessor; // Predecessor of this node
    private final FingerTable fingerTable; // Finger table for routing
    private final int m = 64; // Number of bits for the identifier space
    private NodeHeader currentHeader;
    private MessageStore messageStore;

    public ChordNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.nodeId = hashNode(ip + ":" + port);
        this.fingerTable = new FingerTable(this, m);
        this.currentHeader = new NodeHeader(ip, port, nodeId);
        this.messageStore = new MessageStore();
    }

    // Function to hash the node ID based on its IP and port
    private String hashNode(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(input.getBytes());
            BigInteger hashInt = new BigInteger(1, hashBytes);
            BigInteger mod = BigInteger.valueOf(2).pow(m);
            return hashInt.mod(mod).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Getters and Setters
    public String getNodeId() { return this.nodeId; }
    public String getIp() { return this.ip; }
    public int getPort() { return this.port; }
    public NodeHeader getSuccessor() { return this.successor; }
    public void setSuccessor(NodeHeader successor) { this.successor = successor; }
    public NodeHeader getPredecessor() { return this.predecessor; }
    public void setPredecessor(NodeHeader predecessor) { this.predecessor = predecessor; }
    public FingerTable getFingerTable() { return this.fingerTable; }
    public MessageStore getMessageStore() { return this.messageStore; }

    // Method to join the network
    public void join(String existingNodeIp, int existingNodePort) {
        if (existingNodeIp != null) {
            ChordClient chordClient = new ChordClient(existingNodeIp, existingNodePort);
            initFingerTable(chordClient);
            this.updateOthers();
            chordClient.shutdown();
            // Move appropriate keys from the successor if necessary
        } else {
            // First node in the network
            for (int i = 0; i < m; i++) {
                fingerTable.getFingers().set(i, currentHeader);
            }
            this.predecessor = currentHeader;
            this.successor = currentHeader;
        }
    }

    public void leave() {
        // Last node in the network
        if (this.predecessor.equals(this.currentHeader) && this.successor.equals(this.currentHeader)) {
            // Network is closing
        } else {
            // Transfer keys
            ChordClient predecessorClient = new ChordClient(predecessor.getIp(), Integer.parseInt(predecessor.getPort()));
            ChordClient successorClient = new ChordClient(successor.getIp(), Integer.parseInt(successor.getPort()));
            predecessorClient.setSuccessor(successor);
            successorClient.setPredecessor(predecessor);
            this.successor = null;
            this.predecessor = null;
            this.updateOthers();
            predecessorClient.shutdown();
            successorClient.shutdown();
        }
    }

    // Method to update other nodes' finger tables
    public void updateOthers() {
        BigInteger mod = BigInteger.valueOf(2).pow(m);
        BigInteger nodeIdInt = new BigInteger(this.nodeId);

        for (int i = 0; i < m; i++) {
            BigInteger pow = BigInteger.valueOf(2).pow(i);
            BigInteger idInt = nodeIdInt.subtract(pow).mod(mod);
            String id = idInt.toString();

            NodeHeader predecessorNode = findPredecessor(id);

            ChordClient predecessorClient = new ChordClient(predecessorNode.getIp(), Integer.parseInt(predecessorNode.getPort()));
            predecessorClient.updateFingerTable(this.currentHeader, i);
            predecessorClient.shutdown();
        }
    }

    public void updateFingerTable(NodeHeader s, int i) {
        NodeHeader currentFinger = fingerTable.getFingers().get(i);
        if (currentFinger == null || isInIntervalClosedOpen(s.getNodeId(), this.nodeId, currentFinger.getNodeId())) {
            fingerTable.getFingers().set(i, s);
            NodeHeader p = this.predecessor;
            if (p != null && !p.equals(this.currentHeader)) {
                ChordClient pClient = new ChordClient(p.getIp(), Integer.parseInt(p.getPort()));
                pClient.updateFingerTable(s, i);
                pClient.shutdown();
            }
        }
    }

    // Method to find the successor of a given ID
    public NodeHeader findSuccessor(String id) {
        NodeHeader predecessorNode = findPredecessor(id);

        ChordClient predecessorClient = new ChordClient(predecessorNode.getIp(), Integer.parseInt(predecessorNode.getPort()));
        NodeHeader successor = predecessorClient.getSuccessor();
        predecessorClient.shutdown();

        return successor;
    }

    // Method to find the predecessor of a given ID
    public NodeHeader findPredecessor(String id) {
        NodeHeader nPrime = this.currentHeader;

        while (true) {
            NodeHeader nPrimeSuccessor;

            if (nPrime.equals(this.currentHeader)) {
                nPrimeSuccessor = this.getSuccessor();
            } else {
                ChordClient nPrimeClient = new ChordClient(nPrime.getIp(), Integer.parseInt(nPrime.getPort()));
                nPrimeSuccessor = nPrimeClient.getSuccessor();
                nPrimeClient.shutdown();
            }

            if (isInIntervalOpenClosed(id, nPrime.getNodeId(), nPrimeSuccessor.getNodeId())) {
                return nPrime;
            } else {
                NodeHeader closestFinger;
                if (nPrime.equals(this.currentHeader)) {
                    closestFinger = this.closestPrecedingFinger(id);
                } else {
                    ChordClient nPrimeClient = new ChordClient(nPrime.getIp(), Integer.parseInt(nPrime.getPort()));
                    closestFinger = nPrimeClient.closestPrecedingFinger(id);
                    nPrimeClient.shutdown();
                }

                if (closestFinger.equals(nPrime)) {
                    return nPrime;
                } else {
                    nPrime = closestFinger;
                }
            }
        }
    }

    // Method to find the closest preceding finger
    public NodeHeader closestPrecedingFinger(String id) {
        for (int i = m - 1; i >= 0; i--) {
            NodeHeader fingerNode = fingerTable.getFingers().get(i);
            if (fingerNode != null && isInIntervalOpenOpen(fingerNode.getNodeId(), this.nodeId, id)) {
                return fingerNode;
            }
        }
        return this.currentHeader;
    }

    // Method to stabilize the node
    public void stabilize() throws IOException {
        ChordClient successorClient = new ChordClient(successor.getIp(), Integer.parseInt(successor.getPort()));
        NodeHeader x = successorClient.getPredecessor();

        if (x != null && isInIntervalOpenOpen(x.getNodeId(), this.nodeId, successor.getNodeId())) {
            this.successor = x;
        }

        successorClient.notify(this.currentHeader);
        successorClient.shutdown();
    }

    // Method to notify a node
    public void notify(NodeHeader n) {
        if (this.predecessor == null || isInIntervalOpenOpen(n.getNodeId(), this.predecessor.getNodeId(), this.nodeId)) {
            this.predecessor = n;
        }
    }

    // Method to fix fingers periodically
    public void fixFingers() throws IOException {
        int i = new Random().nextInt(m);
        String start = fingerTable.calculateFingerStart(i);
        NodeHeader successorNode = findSuccessor(start);
        fingerTable.getFingers().set(i, successorNode);
    }

    public void initFingerTable(ChordClient n0Client) {
        // Step 1: Initialize finger[0]
        String start0 = fingerTable.calculateFingerStart(0);
        NodeHeader successorNode = n0Client.findSuccessor(start0);
        fingerTable.getFingers().set(0, successorNode);
        this.setSuccessor(successorNode);

        // Step 2: Set predecessor
        ChordClient successorClient = new ChordClient(successorNode.getIp(), Integer.parseInt(successorNode.getPort()));
        NodeHeader successorPredecessor = successorClient.getPredecessor();
        this.setPredecessor(successorPredecessor);

        successorClient.setPredecessor(this.currentHeader);
        successorClient.shutdown();

        // Step 3: Initialize other entries of the finger table
        for (int i = 1; i < m; i++) {
            String start = fingerTable.calculateFingerStart(i);
            if (isInIntervalClosedOpen(start, this.nodeId, fingerTable.getFingers().get(i - 1).getNodeId())) {
                fingerTable.getFingers().set(i, fingerTable.getFingers().get(i - 1));
            } else {
                NodeHeader successorNodeI = n0Client.findSuccessor(start);
                fingerTable.getFingers().set(i, successorNodeI);
            }
        }
    }

    public void storeMessageInChord(String key, Message message) {
        // Hash the key
        String keyId = hashNode(key);

        // Find the responsible node
        NodeHeader responsibleNode = findSuccessor(keyId);

        // Create a client for the responsible node
        ChordClient responsibleNodeClient = new ChordClient(responsibleNode.getIp(), Integer.parseInt(responsibleNode.getPort()));

        // Store the message on the responsible node
        responsibleNodeClient.storeMessage(key, Wrapper.wrapMessageToGrpcMessage(message));

        responsibleNodeClient.shutdown();
    }

    public Message retrieveMessageFromChord(String key) {
        // Hash the key
        String keyId = hashNode(key);

        // Find the responsible node
        NodeHeader responsibleNode = findSuccessor(keyId);

        // Create a client for the responsible node
        ChordClient responsibleNodeClient = new ChordClient(responsibleNode.getIp(), Integer.parseInt(responsibleNode.getPort()));

        // Retrieve the message from the responsible node
        Message message = Wrapper.wrapGrpcMessageToMessage(responsibleNodeClient.retrieveMessage(key));

        responsibleNodeClient.shutdown();
        return message;
    }

    // Interval checking methods
    private boolean isInIntervalOpenOpen(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);

        if (startInt.equals(endInt)) {
            return !idInt.equals(startInt);
        } else if (startInt.compareTo(endInt) < 0) {
            return idInt.compareTo(startInt) > 0 && idInt.compareTo(endInt) < 0;
        } else {
            return idInt.compareTo(startInt) > 0 || idInt.compareTo(endInt) < 0;
        }
    }

    private boolean isInIntervalOpenClosed(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);

        if (startInt.equals(endInt)) {
            return true;
        } else if (startInt.compareTo(endInt) < 0) {
            return idInt.compareTo(startInt) > 0 && idInt.compareTo(endInt) <= 0;
        } else {
            return idInt.compareTo(startInt) > 0 || idInt.compareTo(endInt) <= 0;
        }
    }

    private boolean isInIntervalClosedOpen(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);

        if (startInt.equals(endInt)) {
            return true;
        } else if (startInt.compareTo(endInt) < 0) {
            return idInt.compareTo(startInt) >= 0 && idInt.compareTo(endInt) < 0;
        } else {
            return idInt.compareTo(startInt) >= 0 || idInt.compareTo(endInt) < 0;
        }
    }
}
