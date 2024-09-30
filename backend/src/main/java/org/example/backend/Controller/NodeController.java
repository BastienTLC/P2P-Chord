package org.example.backend.Controller;


import org.example.backend.Entity.Node;
import org.example.backend.Services.NodeServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/node")
@CrossOrigin(origins = "*")
public class NodeController {


    @GetMapping("/info/{host}/{port}")
    public Node getNodeInfo(@PathVariable String host, @PathVariable int port) {
        NodeServices nodeServices = new NodeServices(host, port);
        return nodeServices.getChordNodeInfo();
    }

    @PatchMapping("/run/{host}/{port}")
    public boolean runNode(@PathVariable String host, @PathVariable int port) {
        NodeServices nodeServices = new NodeServices(host, port);
        return nodeServices.runNode(host, port, "localhost", 8000);
    }

    @PatchMapping("/stop/{host}/{port}")
    public boolean stopNode(@PathVariable String host, @PathVariable int port) {
        NodeServices nodeServices = new NodeServices(host, port);
        return nodeServices.stopNode();
    }

}
