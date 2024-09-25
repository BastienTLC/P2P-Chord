package org.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeHeader {
    @JsonProperty("id")
    private String id;

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("port")
    private int port;

    public NodeHeader(String ip, int port, String id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}