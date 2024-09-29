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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}