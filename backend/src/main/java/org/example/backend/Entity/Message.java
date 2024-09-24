package org.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("id")
    private String id;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("author")
    private String author;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("content")
    private String content;

    @JsonProperty("data")
    private byte[] data;

    public Message(String id, long timestamp, String author, String topic, String content, byte[] data) {
        this.id = id;
        this.timestamp = timestamp;
        this.author = author;
        this.topic = topic;
        this.content = content;
        this.data = data;
    }
}