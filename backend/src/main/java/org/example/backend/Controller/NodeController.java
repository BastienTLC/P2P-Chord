package org.example.backend.Controller;


import org.example.backend.Entity.Node;
import org.example.backend.Services.NodeServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/node")
public class NodeController {


    @GetMapping("/info/{host}/{port}")
    public Node getNodeInfo(@PathVariable String host, @PathVariable int port) {
        NodeServices nodeServices = new NodeServices(host, port);
        return nodeServices.getChordNodeInfo();
    }

}
