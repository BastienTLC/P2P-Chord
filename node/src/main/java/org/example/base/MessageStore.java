package org.example.base;

import java.util.HashMap;
import java.util.Map;

public class MessageStore {
    private final Map<String, String> storage;

    public MessageStore() {
        this.storage = new HashMap<>();
    }

    public void storeMessage(String key, String content) {
        storage.put(key, content);
    }

    public String retrieveMessage(String key) {
        return storage.get(key);
    }
}
