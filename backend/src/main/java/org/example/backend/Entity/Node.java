package org.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

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
}