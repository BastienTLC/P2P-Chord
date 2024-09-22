package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.base.ChordNode;
import org.example.base.ChordServiceImpl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        ChordNode node = new ChordNode("localhost", 50051);
        ChordServiceImpl service = new ChordServiceImpl(node);

        Server server = ServerBuilder.forPort(50051)
                .addService(service)
                .build();

        System.out.println("Nœud " + node.getNodeId() + " démarré sur le port 50051");
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
