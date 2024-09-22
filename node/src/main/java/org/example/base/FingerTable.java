package org.example.base;

import org.example.base.ChordNode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class FingerTable {
    private final List<ChordNode> fingers;
    private final ChordNode localNode;
    private final int m = 5;

    public FingerTable(ChordNode localNode) {
        this.localNode = localNode;
        this.fingers = new ArrayList<>(Collections.nCopies(m, null));
    }

    // Initialisation de la Finger Table basée sur un nœud
    public void initFingerTable(ChordNode n0) throws IOException {
        fingers.set(0, n0.findSuccessor(localNode.getNodeId()));
        localNode.setSuccessor(fingers.get(0));

        // Définir le prédécesseur
        localNode.setPredecessor(localNode.getSuccessor().getPredecessor());
        localNode.getSuccessor().setPredecessor(localNode);

        for (int i = 1; i < m; i++) {
            String fingerStart = calculateFingerStart(i);
            if (isInInterval(fingerStart, localNode.getNodeId(), fingers.get(i - 1).getNodeId())) {
                fingers.set(i, fingers.get(i - 1));
            } else {
                fingers.set(i, n0.findSuccessor(fingerStart));
            }
        }
    }

    // Méthode pour calculer le début de la plage de la i-ème entrée
    private String calculateFingerStart(int i) {
        BigInteger nodeHash = new BigInteger(localNode.getNodeId());
        BigInteger start = nodeHash.add(BigInteger.valueOf((long) Math.pow(2, i))).mod(BigInteger.valueOf((long) Math.pow(2, m)));
        return start.toString();
    }

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

    public ChordNode closestPrecedingFinger(String key) {
        for (int i = m - 1; i >= 0; i--) {
            if (fingers.get(i) != null && isInInterval(fingers.get(i).getNodeId(), this.localNode.getNodeId(), key)) {
                return fingers.get(i);
            }
        }
        return this.localNode;
    }

    public List<ChordNode> getFingers() {
        return fingers;
    }

    public int getM() {
        return m;
    }
}
