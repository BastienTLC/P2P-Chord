package org.example.base;

import org.example.types.NodeHeader;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ChordNode {
    private final String ip;
    private final int port;
    private final String nodeId; // ID unique du nœud basé sur le hash de l'IP et du port
    private NodeHeader successor; // Le successeur de ce nœud
    private NodeHeader predecessor; // Le prédécesseur de ce nœud
    private final FingerTable fingerTable; // La table de doigts pour le routage
    private final int m = 5; // Nombre de bits pour l'espace d'identifiants
    private NodeHeader currentHeader;

    public ChordNode(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.nodeId = hashNode(ip + ":" + port);
        this.fingerTable = new FingerTable(this);
        this.currentHeader = new NodeHeader(ip, port, nodeId);
    }

    // Fonction pour hacher l'ID du nœud basé sur son IP et son port
    private String hashNode(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(input.getBytes());
        BigInteger hashInt = new BigInteger(1, hashBytes);
        BigInteger mod = BigInteger.valueOf(2).pow(m);
        return hashInt.mod(mod).toString();
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

    // Méthode join
    public void join(String existingNodeIp, int existingNodePort) throws IOException {
        if (existingNodeIp != null) {
            ChordClient chordClient = new ChordClient(existingNodeIp, existingNodePort);
            fingerTable.initFingerTable(chordClient);
            this.updateOthers();
            // Déplacer les clés appropriées du successeur si nécessaire
        } else {
            // Premier nœud dans le réseau
            for (int i = 0; i < m; i++) {
                fingerTable.getFingers().set(i, currentHeader);
            }
            this.predecessor = currentHeader;
            this.successor = currentHeader;
        }
    }

    // Méthode updateOthers
    public void updateOthers() throws IOException {
        BigInteger twoPowerM = BigInteger.valueOf(2).pow(m);
        for (int i = 0; i < m; i++) {
            BigInteger offset = BigInteger.valueOf(2).pow(i);
            BigInteger id = new BigInteger(this.nodeId).subtract(offset).mod(twoPowerM);
            String idStr = id.toString();
            NodeHeader p = findPredecessor(idStr);
            if (p != null) {
                ChordClient pClient = new ChordClient(p.getIp(), Integer.parseInt(p.getPort()));
                pClient.updateFingerTable(currentHeader, i);
            }
        }
    }

    // Méthode updateFingerTable
    public void updateFingerTable(NodeHeader s, int i) {
        NodeHeader currentFinger = fingerTable.getFingers().get(i);
        if (currentFinger == null || isInIntervalOpenClosed(s.getNodeId(), this.nodeId, currentFinger.getNodeId())) {
            fingerTable.getFingers().set(i, s);
            NodeHeader p = this.predecessor;
            if (p != null) {
                ChordClient pClient = new ChordClient(p.getIp(), Integer.parseInt(p.getPort()));
                pClient.updateFingerTable(s, i);
            }
        }
    }

    // Méthode findSuccessor
    public NodeHeader findSuccessor(String id) throws IOException {
        NodeHeader n0 = this.findPredecessor(id);
        if (n0.equals(this)) {
            return this.successor;
        } else {
            ChordClient n0Client = new ChordClient(n0.getIp(), Integer.parseInt(n0.getPort()));
            return n0Client.getSuccessor();
        }
    }

    // Méthode findPredecessor
    public NodeHeader findPredecessor(String id) throws IOException {
        ChordNode n0 = this;
        NodeHeader n0Predecessor = this.predecessor;
        while (!isInIntervalOpenClosed(id, n0.getNodeId(), n0.getSuccessor().getNodeId())) {
            NodeHeader closestFinger = n0.closestPrecedingFinger(id);
            if (closestFinger.equals(n0)) {
                break;
            }
            ChordClient fingerClient = new ChordClient(closestFinger.getIp(), Integer.parseInt(closestFinger.getPort()));
            n0Predecessor = fingerClient.getNodeInfo();
        }
        return n0Predecessor;
    }

    // Méthode closestPrecedingFinger
    public NodeHeader closestPrecedingFinger(String id) {
        for (int i = m - 1; i >= 0; i--) {
            NodeHeader fingerNode = fingerTable.getFingers().get(i);
            if (fingerNode != null && isInIntervalOpenOpen(fingerNode.getNodeId(), this.nodeId, id)) {
                return fingerNode;
            }
        }
        return currentHeader;
    }

    // Fonctions d'intervalle
    private boolean isInIntervalOpenClosed(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);
        if (startInt.compareTo(endInt) < 0) {
            return idInt.compareTo(startInt) > 0 && idInt.compareTo(endInt) <= 0;
        } else if (startInt.compareTo(endInt) > 0) {
            return idInt.compareTo(startInt) > 0 || idInt.compareTo(endInt) <= 0;
        } else {
            return true;
        }
    }

    private boolean isInIntervalOpenOpen(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);
        if (startInt.compareTo(endInt) < 0) {
            return idInt.compareTo(startInt) > 0 && idInt.compareTo(endInt) < 0;
        } else if (startInt.compareTo(endInt) > 0) {
            return idInt.compareTo(startInt) > 0 || idInt.compareTo(endInt) < 0;
        } else {
            return false;
        }
    }

    // Méthode stabilize
    public void stabilize() throws IOException {
        ChordClient successorClient = new ChordClient(successor.getIp(), Integer.parseInt(successor.getPort()));
        NodeHeader x = successorClient.getPredecessor(successor);
        if (x != null && isInIntervalOpenOpen(x.getNodeId(), this.nodeId, successor.getNodeId())) {
            this.successor = x;
        }
        successorClient.notify(this);
    }

    // Méthode notify
    public void notify(NodeHeader n) {
        if (this.predecessor == null || isInIntervalOpenOpen(n.getNodeId(), this.predecessor.getNodeId(), this.nodeId)) {
            this.predecessor = n;
        }
    }

    // Méthode fixFingers
    public void fixFingers() throws IOException {
        int i = new Random().nextInt(m - 1) + 1;
        String start = fingerTable.calculateFingerStart(i);
        NodeHeader successorNode = findSuccessor(start);
        fingerTable.getFingers().set(i, successorNode);
    }
}
