package org.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Node {
    @JsonProperty("ip")
    private String ip;

    @JsonProperty("port")
    private int port;

    @JsonProperty("id")
    private String id;

    @JsonProperty("predecessor")
    private NodeHeader predecessor;

    @JsonProperty("successor")
    private NodeHeader successor;

    @JsonProperty("fingerTable")
    private FingerTable fingerTable;

    @JsonProperty("m")
    private int m;

    @JsonProperty("messageStore")
    private Message[] messageStore;

    public Node(String ip, int port, String id, NodeHeader predecessor, NodeHeader successor, FingerTable fingerTable, int m, Message[] messageStore) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.predecessor = predecessor;
        this.successor = successor;
        this.fingerTable = fingerTable;
        this.m = m;
        this.messageStore = messageStore;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeHeader getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(NodeHeader predecessor) {
        this.predecessor = predecessor;
    }

    public NodeHeader getSuccessor() {
        return successor;
    }

    public void setSuccessor(NodeHeader successor) {
        this.successor = successor;
    }

    public FingerTable getFingerTable() {
        return fingerTable;
    }

    public void setFingerTable(FingerTable fingerTable) {
        this.fingerTable = fingerTable;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public Message[] getMessageStore() {
        return messageStore;
    }

    public void setMessageStore(Message[] messageStore) {
        this.messageStore = messageStore;
    }
}