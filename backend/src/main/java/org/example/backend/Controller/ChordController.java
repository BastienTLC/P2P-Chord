package org.example.backend.Controller;

import org.example.backend.Entity.Message;
import org.example.backend.Entity.Node;
import org.example.backend.Services.NodeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/node")
public class ChordController {
    private final String host = "localhost";
    private final int port = 8000;
    private final NodeServices nodeServices = new NodeServices(host, port);


    @GetMapping("/info")
    public Node getNodeInfo() {
        return nodeServices.getChordNodeInfo();
    }
}