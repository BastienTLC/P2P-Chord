package org.example.base;

import org.example.types.Message;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.example.base.FingerTable;

public class ChordNode {
    private final String ip;
    private final int port;
    private final String nodeId; // ID unique du nœud basé sur le hash de l'IP et du port
    private ChordNode successor; // Le successeur de ce nœud
    private ChordNode predecessor; // Le prédécesseur de ce nœud
    private final FingerTable fingerTable; // La table de doigts pour le routage
    private final MessageStore messageStore; // Le stockage des messages (DHT)

    public ChordNode(String ip, int port) throws NoSuchAlgorithmException {
        this.ip = ip;
        this.port = port;
        this.nodeId = hashNode(ip + ":" + port); // Générer l'ID du nœud
        this.fingerTable = new FingerTable(this); // Crée une Finger Table pour ce nœud
        this.messageStore = new MessageStore(); // Initialisation du stockage pour la DHT
    }

    // Fonction pour hacher l'ID du nœud basé sur son IP et son port
    private String hashNode(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(input.getBytes());
        return new BigInteger(1, hashBytes).mod(BigInteger.valueOf(32)).toString(); // Mod 32 pour l'anneau
    }

    // Retourne l'ID du nœud
    public String getNodeId() {
        return this.nodeId;
    }

    // Retourne l'adresse IP du nœud
    public String getIp() {
        return this.ip;
    }

    // Retourne le port du nœud
    public int getPort() {
        return this.port;
    }

    // Retourne le successeur de ce nœud
    public ChordNode getSuccessor() {
        return this.successor;
    }

    // Définit le successeur de ce nœud
    public void setSuccessor(ChordNode successor) {
        this.successor = successor;
    }

    // Retourne le prédécesseur de ce nœud
    public ChordNode getPredecessor() {
        return this.predecessor;
    }

    // Définit le prédécesseur de ce nœud
    public void setPredecessor(ChordNode predecessor) {
        this.predecessor = predecessor;
    }

    // Retourne la Finger Table associée à ce nœud
    public FingerTable getFingerTable() {
        return this.fingerTable;
    }

    // Retourne l'objet MessageStore pour gérer les messages
    public MessageStore getMessageStore() {
        return this.messageStore;
    }

    public void join(ChordNode existingNode) throws IOException {
        if (existingNode != null) {
            fingerTable.initFingerTable(existingNode);
            updateOthers();
        } else {
            // Premier nœud dans le réseau
            for (int i = 0; i < fingerTable.getM(); i++) {
                fingerTable.getFingers().set(i, this);
            }
            this.predecessor = this;
            this.successor = this;
        }
    }

    public void leave(String nodeId) throws IOException {
        ChordNode n0 = this.findPredecessor(nodeId);
        ChordNode n0Successor = n0.getSuccessor();
        n0.setSuccessor(n0Successor.getSuccessor());
        n0Successor.setPredecessor(n0);
        this.updateOthers();
    }

    public boolean ping(String nodeId) throws IOException {
        ChordNode n0 = this.findPredecessor(nodeId);
        return n0.getPredecessor() != null;

    }

    // Méthode pour trouver le successeur d'une clé donnée
    public ChordNode findSuccessor(String key) throws IOException {
        ChordNode n0 = this.findPredecessor(key);
        return n0.getSuccessor();
    }

    // Méthode pour trouver le prédécesseur d'une clé donnée
    public ChordNode findPredecessor(String key) throws IOException {
        ChordNode n0 = this;
        ChordNode n0Successor = n0.getSuccessor();
        while (!isInInterval(key, n0.getNodeId(), n0Successor.getNodeId())) {
            n0 = fingerTable.closestPrecedingFinger(key);
            n0Successor = n0.getSuccessor();
        }
        return n0;
    }


    // Fonction de stabilisation : vérifie et met à jour le successeur et le prédécesseur
    public void stabilize() throws IOException {
        ChordNode x = successor.getPredecessor();
        if (x != null && isInInterval(x.getNodeId(), this.nodeId, successor.getNodeId())) {
            this.successor = x;
        }
        successor.notify(this);
    }

    // Notifie le nœud actuel qu'il pourrait être le prédécesseur d'un autre nœud
    public void notify(ChordNode n) {
        if (this.predecessor == null || isInInterval(n.getNodeId(), this.predecessor.getNodeId(), this.nodeId)) {
            this.predecessor = n;
        }
    }

    // Méthode pour vérifier si une clé est dans l'intervalle [start, end]
    private boolean isInInterval(String key, String start, String end) {
        BigInteger keyHash = new BigInteger(key);
        BigInteger startHash = new BigInteger(start);
        BigInteger endHash = new BigInteger(end);

        if (startHash.compareTo(endHash) < 0) {
            return keyHash.compareTo(startHash) > 0 && keyHash.compareTo(endHash) <= 0;
        } else {
            return keyHash.compareTo(startHash) > 0 || keyHash.compareTo(endHash) <= 0;
        }
    }

    // Méthode pour corriger les Finger Tables en vérifiant aléatoirement
    public void fixFingers() throws IOException {
        int i = new Random().nextInt(fingerTable.getM() - 1) + 1;
        this.updateFingerTable(this,i);
    }

    public void updateFingerTable(ChordNode node ,int i) {
        if (isInInterval(this.nodeId, this.nodeId, fingerTable.getFingers().get(i).getNodeId())) {
            fingerTable.getFingers().set(i, fingerTable.getFingers().get(i));
            ChordNode p = this.predecessor;
            if (p != null) {
                p.updateFingerTable(node, i);
            }
        }
    }

    // Mise à jour des Finger Tables des autres nœuds affectés
    public void updateOthers() throws IOException {
        for (int i = 0; i < this.fingerTable.getM(); i++) {
            ChordNode p = findPredecessor(new BigInteger(this.nodeId).subtract(BigInteger.valueOf((long) Math.pow(2, i))).toString());
            p.updateFingerTable(this, i);
        }
    }
}

