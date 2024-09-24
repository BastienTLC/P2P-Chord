package org.example.base;

import org.example.types.NodeHeader;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FingerTable {
    private final List<NodeHeader> fingers;
    private final ChordNode localNode;
    private final int m = 5; // Nombre de bits pour l'espace d'identifiants

    public FingerTable(ChordNode localNode) {
        this.localNode = localNode;
        this.fingers = new ArrayList<>(Collections.nCopies(m, null));
    }

    // Initialisation de la Finger Table basée sur un nœud existant
    public void initFingerTable(ChordClient n0Client) throws IOException {
        // Étape 1 : Initialiser finger[0]
        String start0 = calculateFingerStart(0);
        NodeHeader successorNode = n0Client.findSuccessor(start0);
        fingers.set(0, successorNode);
        localNode.setSuccessor(successorNode);

        // Étape 2 : Définir le prédécesseur
        ChordClient successorClient = new ChordClient(successorNode.getIp(), Integer.parseInt(successorNode.getPort()));
        NodeHeader successorPredecessor = successorClient.getPredecessor(successorNode);
        localNode.setPredecessor(successorPredecessor);

        successorClient.setPredecessor(localNode);


        // Étape 3 : Initialiser les autres entrées de la Finger Table
        for (int i = 1; i < m; i++) {
            String start = calculateFingerStart(i);
            if (isInIntervalOpenClosed(start, localNode.getNodeId(), fingers.get(i - 1).getNodeId())) {
                fingers.set(i, fingers.get(i - 1));
            } else {
                NodeHeader successorNodeI = n0Client.findSuccessor(start);
                fingers.set(i, successorNodeI);
            }
        }
        successorClient.shutdown();
    }

    // Méthode pour calculer le début de la plage de la i-ème entrée
    public String calculateFingerStart(int i) {
        BigInteger nodeHash = new BigInteger(localNode.getNodeId());
        BigInteger twoPowerI = BigInteger.valueOf(2).pow(i);
        BigInteger twoPowerM = BigInteger.valueOf(2).pow(m);
        BigInteger start = nodeHash.add(twoPowerI).mod(twoPowerM);
        return start.toString();
    }

    // Méthode isInInterval pour les intervalles ouverts-fermés
    private boolean isInIntervalOpenClosed(String key, String start, String end) {
        BigInteger keyHash = new BigInteger(key);
        BigInteger startHash = new BigInteger(start);
        BigInteger endHash = new BigInteger(end);

        if (startHash.compareTo(endHash) < 0) {
            return keyHash.compareTo(startHash) > 0 && keyHash.compareTo(endHash) <= 0;
        } else if (startHash.compareTo(endHash) > 0) {
            return keyHash.compareTo(startHash) > 0 || keyHash.compareTo(endHash) <= 0;
        } else {
            return true;
        }
    }

    // Méthode isInInterval pour les intervalles ouverts-ouverts
    private boolean isInIntervalOpenOpen(String key, String start, String end) {
        BigInteger keyHash = new BigInteger(key);
        BigInteger startHash = new BigInteger(start);
        BigInteger endHash = new BigInteger(end);

        if (startHash.compareTo(endHash) < 0) {
            return keyHash.compareTo(startHash) > 0 && keyHash.compareTo(endHash) < 0;
        } else if (startHash.compareTo(endHash) > 0) {
            return keyHash.compareTo(startHash) > 0 || keyHash.compareTo(endHash) < 0;
        } else {
            return false;
        }
    }

    public List<NodeHeader> getFingers() { return fingers; }
    public int getM() { return m; }
}
