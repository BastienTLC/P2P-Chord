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
    private final String nodeId; // ID unique du nœud basé sur le hash de l'IP et du port
    private NodeHeader successor; // Le successeur de ce nœud
    private NodeHeader predecessor; // Le prédécesseur de ce nœud
    private final FingerTable fingerTable; // La table de doigts pour le routage
    private final int m = 3; // Nombre de bits pour l'espace d'identifiants
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

    // Fonction pour hacher l'ID du nœud basé sur son IP et son port
    private String hashNode(String input)  {
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

    // Méthode join
    public void join(String existingNodeIp, int existingNodePort) throws IOException {
        System.out.println("[" + this.nodeId + "] - Rejoindre le réseau via le nœud : " + existingNodeIp + ":" + existingNodePort);

        if (existingNodeIp != null) {
            ChordClient chordClient = new ChordClient(existingNodeIp, existingNodePort);
            initFingerTable(chordClient);
            this.updateOthers();
            chordClient.shutdown();
            // Déplacer les clés appropriées du successeur si nécessaire
        } else {
            // Premier nœud dans le réseau
            System.out.println("[" + this.nodeId + "] - Premier nœud dans le réseau. Initialisation de la table de doigts.");
            for (int i = 0; i < m; i++) {
                fingerTable.getFingers().set(i, currentHeader);
            }
            this.predecessor = currentHeader;
            this.successor = currentHeader;
        }

        // Afficher l'état du nœud après la jointure
        System.out.println("[" + this.nodeId + "] - État après la jointure :");
        printNodeState();
    }


    // Méthode updateOthers
    public void updateOthers() throws IOException {
        BigInteger mod = BigInteger.valueOf(2).pow(m);
        BigInteger nodeIdInt = new BigInteger(this.nodeId);

        for (int i = 1; i <= m; i++) {
            BigInteger pow = BigInteger.valueOf(2).pow(i - 1);
            BigInteger idInt = nodeIdInt.subtract(pow).mod(mod);
            String id = idInt.toString();

            NodeHeader predecessorNode = findPredecessor(id);

            ChordClient predecessorClient = new ChordClient(predecessorNode.getIp(), Integer.parseInt(predecessorNode.getPort()));
            predecessorClient.updateFingerTable(this.currentHeader, i);
            predecessorClient.shutdown();
        }
    }



    // Méthode updateFingerTable
    public void updateFingerTable(NodeHeader s, int i) {
        System.out.println("[" + this.nodeId + "] - updateFingerTable appelé avec s = " + s.getNodeId() + ", i = " + i);
        NodeHeader currentFinger = fingerTable.getFingers().get(i - 1);
        if (currentFinger == null || isInIntervalClosedOpen(s.getNodeId(), this.nodeId, currentFinger.getNodeId())) {
            fingerTable.getFingers().set(i - 1, s);
            System.out.println("[" + this.nodeId + "] - finger[" + (i - 1) + "] mis à jour avec " + s.getNodeId());
            NodeHeader p = this.predecessor;
            if (p != null && !p.equals(this.currentHeader)) {
                System.out.println("[" + this.nodeId + "] - Appel récursif à updateFingerTable sur le prédécesseur " + p.getNodeId());
                ChordClient pClient = new ChordClient(p.getIp(), Integer.parseInt(p.getPort()));
                pClient.updateFingerTable(s, i);
                pClient.shutdown();
            }
        } else {
            System.out.println("[" + this.nodeId + "] - Aucun besoin de mettre à jour finger[" + (i - 1) + "]");
        }
    }



    // Méthode findSuccessor
    public NodeHeader findSuccessor(String id) {
        System.out.println("[" + this.nodeId + "] - findSuccessor appelé avec id = " + id);
        NodeHeader predecessorNode = findPredecessor(id);
        System.out.println("[" + this.nodeId + "] - Prédécesseur trouvé : " + predecessorNode.getNodeId());

        ChordClient predecessorClient = new ChordClient(predecessorNode.getIp(), Integer.parseInt(predecessorNode.getPort()));
        NodeHeader successor = predecessorClient.getSuccessor();
        predecessorClient.shutdown();

        System.out.println("[" + this.nodeId + "] - Successeur du prédécesseur : " + successor.getNodeId());
        return successor;
    }


    // Méthode findPredecessor
    public NodeHeader findPredecessor(String id) {
        System.out.println("[" + this.nodeId + "] - findPredecessor appelé avec id = " + id);
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

            System.out.println("[" + this.nodeId + "] - n' = " + nPrime.getNodeId() + ", n'.successeur = " + nPrimeSuccessor.getNodeId());

            if (isInIntervalOpenClosed(id, nPrime.getNodeId(), nPrimeSuccessor.getNodeId())) {
                System.out.println("[" + this.nodeId + "] - id est dans l'intervalle (" + nPrime.getNodeId() + ", " + nPrimeSuccessor.getNodeId() + "]");
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

                System.out.println("[" + this.nodeId + "] - closestPrecedingFinger pour id " + id + " est " + closestFinger.getNodeId());

                if (closestFinger.equals(nPrime)) {
                    System.out.println("[" + this.nodeId + "] - closestPrecedingFinger est égal à n', retour de n'");
                    return nPrime;
                } else {
                    nPrime = closestFinger;
                }
            }
        }
    }


    // Méthode closestPrecedingFinger
    public NodeHeader closestPrecedingFinger(String id) {
        System.out.println("[" + this.nodeId + "] - closestPrecedingFinger appelé avec id = " + id);
        for (int i = m - 1; i >= 0; i--) {
            NodeHeader fingerNode = fingerTable.getFingers().get(i);
            if (fingerNode != null && isInIntervalOpenOpen(fingerNode.getNodeId(), this.nodeId, id)) {
                System.out.println("[" + this.nodeId + "] - finger[" + i + "] = " + fingerNode.getNodeId() + " est le plus proche prédécesseur de " + id);
                return fingerNode;
            }
        }
        System.out.println("[" + this.nodeId + "] - Aucun finger trouvé, retourne le nœud courant");
        return this.currentHeader;
    }

    // Méthode stabilize
    public void stabilize() throws IOException {
        System.out.println("[" + this.nodeId + "] - Stabilisation en cours...");
        ChordClient successorClient = new ChordClient(successor.getIp(), Integer.parseInt(successor.getPort()));
        NodeHeader x = successorClient.getPredecessor();

        System.out.println("[" + this.nodeId + "] - Prédécesseur de mon successeur (" + successor.getNodeId() + ") est " + (x != null ? x.getNodeId() : "null"));

        if (x != null && isInIntervalOpenOpen(x.getNodeId(), this.nodeId, successor.getNodeId())) {
            this.successor = x;
            System.out.println("[" + this.nodeId + "] - Mon successeur est mis à jour à " + x.getNodeId());
        }

        successorClient.notify(this.currentHeader);
        successorClient.shutdown();
    }




    // Méthode notify
    public void notify(NodeHeader n) {
        System.out.println("[" + this.nodeId + "] - Reçu une notification de " + n.getNodeId());
        if (this.predecessor == null || isInIntervalOpenOpen(n.getNodeId(), this.predecessor.getNodeId(), this.nodeId)) {
            this.predecessor = n;
            System.out.println("[" + this.nodeId + "] - Mon prédécesseur est mis à jour à " + n.getNodeId());
        } else {
            System.out.println("[" + this.nodeId + "] - Aucun besoin de mettre à jour mon prédécesseur");
        }
    }


    // Méthode fixFingers
    public void fixFingers() throws IOException {
        int i = new Random().nextInt(m - 1) + 1;
        String start = fingerTable.calculateFingerStart(i);
        NodeHeader successorNode = findSuccessor(start);
        fingerTable.getFingers().set(i, successorNode);
    }

    public void initFingerTable(ChordClient n0Client) throws IOException {
        System.out.println("[" + this.nodeId + "] - Initialisation de la table de doigts.");
        // Étape 1 : Initialiser finger[0]
        String start0 = fingerTable.calculateFingerStart(0);
        NodeHeader successorNode = n0Client.findSuccessor(start0);
        fingerTable.getFingers().set(0, successorNode);
        this.setSuccessor(successorNode);
        System.out.println("[" + this.nodeId + "] - finger[0] initialisé à " + successorNode.getNodeId());

        // Étape 2 : Définir le prédécesseur
        ChordClient successorClient = new ChordClient(successorNode.getIp(), Integer.parseInt(successorNode.getPort()));
        NodeHeader successorPredecessor = successorClient.getPredecessor();
        this.setPredecessor(successorPredecessor);
        System.out.println("[" + this.nodeId + "] - Prédécesseur défini à " + (successorPredecessor != null ? successorPredecessor.getNodeId() : "null"));

        successorClient.setPredecessor(this.currentHeader);
        successorClient.shutdown();

        // Étape 3 : Initialiser les autres entrées de la Table de Doigts
        for (int i = 1; i < m; i++) {
            String start = fingerTable.calculateFingerStart(i);
            if (isInIntervalClosedOpen(start, this.nodeId, fingerTable.getFingers().get(i - 1).getNodeId())) {
                fingerTable.getFingers().set(i, fingerTable.getFingers().get(i - 1));
                System.out.println("[" + this.nodeId + "] - finger[" + i + "] initialisé à " + fingerTable.getFingers().get(i).getNodeId() + " (copié de finger[" + (i - 1) + "])");
            } else {
                NodeHeader successorNodeI = n0Client.findSuccessor(start);
                fingerTable.getFingers().set(i, successorNodeI);
                System.out.println("[" + this.nodeId + "] - finger[" + i + "] initialisé à " + successorNodeI.getNodeId() + " (trouvé via findSuccessor)");
            }
        }
    }

    public void storeMessageInChord(String key, Message message)  {
        // Calculer le hachage de la clé
        String keyId = hashNode(key);

        // Trouver le successeur responsable de la clé
        NodeHeader responsibleNode = findSuccessor(keyId);

        // Créer un client pour le nœud responsable
        ChordClient responsibleNodeClient = new ChordClient(responsibleNode.getIp(), Integer.parseInt(responsibleNode.getPort()));

        // Appeler storeMessage sur le nœud responsable
        boolean success = responsibleNodeClient.storeMessage(key, Wrapper.wrapMessageToGrpcMessage(message));

        responsibleNodeClient.shutdown();
    }

    public Message retrieveMessageFromChord(String key) {
        // Calculer le hachage de la clé

        // Trouver le successeur responsable de la clé
        NodeHeader responsibleNode = findSuccessor(key);

        // Créer un client pour le nœud responsable
        ChordClient responsibleNodeClient = new ChordClient(responsibleNode.getIp(), Integer.parseInt(responsibleNode.getPort()));

        // Appeler retrieveMessage sur le nœud responsable
        Message message = Wrapper.wrapGrpcMessageToMessage(responsibleNodeClient.retrieveMessage(key));

        responsibleNodeClient.shutdown();
        return message;
    }


    private boolean isInIntervalOpenOpen(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);
        BigInteger mod = BigInteger.valueOf(2).pow(m);

        boolean result;
        if (startInt.equals(endInt)) {
            // L'intervalle (n, n) couvre tout le cercle sauf le point n
            result = !idInt.equals(startInt);
        } else if (startInt.compareTo(endInt) < 0) {
            result = idInt.compareTo(startInt) > 0 && idInt.compareTo(endInt) < 0;
        } else {
            // L'intervalle est circulaire
            result = idInt.compareTo(startInt) > 0 || idInt.compareTo(endInt) < 0;
        }
        System.out.println("isInIntervalOpenOpen: id " + id + " ∈ (" + start + ", " + end + ") ? " + result);
        return result;
    }

    private boolean isInIntervalOpenClosed(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);
        BigInteger mod = BigInteger.valueOf(2).pow(m);

        boolean result;
        if (startInt.equals(endInt)) {
            // L'intervalle (n, n] couvre tout le cercle
            result = true;
        } else if (startInt.compareTo(endInt) < 0) {
            result = idInt.compareTo(startInt) > 0 && idInt.compareTo(endInt) <= 0;
        } else {
            // L'intervalle est circulaire
            result = idInt.compareTo(startInt) > 0 || idInt.compareTo(endInt) <= 0;
        }
        System.out.println("isInIntervalOpenClosed: id " + id + " ∈ (" + start + ", " + end + "] ? " + result);
        return result;
    }

    private boolean isInIntervalClosedOpen(String id, String start, String end) {
        BigInteger idInt = new BigInteger(id);
        BigInteger startInt = new BigInteger(start);
        BigInteger endInt = new BigInteger(end);
        BigInteger mod = BigInteger.valueOf(2).pow(m);

        boolean result;
        if (startInt.equals(endInt)) {
            // L'intervalle [n, n) couvre tout le cercle
            result = true;
        } else if (startInt.compareTo(endInt) < 0) {
            result = idInt.compareTo(startInt) >= 0 && idInt.compareTo(endInt) < 0;
        } else {
            // L'intervalle est circulaire
            result = idInt.compareTo(startInt) >= 0 || idInt.compareTo(endInt) < 0;
        }
        System.out.println("isInIntervalClosedOpen: id " + id + " ∈ [" + start + ", " + end + ") ? " + result);
        return result;
    }

    public void printNodeState() {
        System.out.println("[" + this.nodeId + "] - État du nœud :");
        System.out.println("  Prédécesseur : " + (predecessor != null ? predecessor.getNodeId() : "null"));
        System.out.println("  Successeur : " + (successor != null ? successor.getNodeId() : "null"));
        System.out.println("  Table de doigts :");
        for (int i = 0; i < fingerTable.getFingers().size(); i++) {
            NodeHeader finger = fingerTable.getFingers().get(i);
            System.out.println("    finger[" + i + "] = " + (finger != null ? finger.getNodeId() : "null"));
        }
    }




}
