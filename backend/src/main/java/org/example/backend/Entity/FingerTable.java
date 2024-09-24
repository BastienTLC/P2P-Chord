package org.example.backend.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FingerTable {
    @JsonProperty("finger")
    private List<NodeHeader> finger;

    public FingerTable(List<NodeHeader> finger) {
        this.finger = finger;
    }
}