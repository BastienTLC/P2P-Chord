package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.base.ChordNode;
import org.example.base.ChordServiceImpl;
import org.example.base.ScheduledTask;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        // Valeurs par défaut
        String host = "localhost";
        int port = 50051;
        String existingNodeIp = null;
        int existingNodePort = -1;

        // Parsing des arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-host") && i + 1 < args.length) {
                host = args[i + 1];
            } else if (args[i].equals("-port") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Le port spécifié est invalide : " + args[i + 1]);
                    System.exit(1);
                }
            } else if (args[i].equals("-joinIp") && i + 1 < args.length) {
                existingNodeIp = args[i + 1];
            } else if (args[i].equals("-joinPort") && i + 1 < args.length) {
                try {
                    existingNodePort = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Le port du nœud à rejoindre est invalide : " + args[i + 1]);
                    System.exit(1);
                }
            }
        }

        // Création du nœud Chord
        ChordNode node = new ChordNode(host, port);
        ScheduledTask scheduledTask = new ScheduledTask(node);

        // Lancement du service gRPC
        ChordServiceImpl service = new ChordServiceImpl(node);
        Server server = ServerBuilder.forPort(port)
                .addService(service)
                .build();

        server.start();

        System.out.println("Nœud " + node.getNodeId() + " démarré sur " + host + ":" + port);

        // Rejoindre le réseau
        if (existingNodeIp != null && existingNodePort != -1) {
            // Rejoindre le réseau via un nœud existant
            node.join(existingNodeIp, existingNodePort);
            System.out.println("Nœud a rejoint le réseau via " + existingNodeIp + ":" + existingNodePort);
        } else {
            // Premier nœud du réseau
            node.join(null, -1);
            System.out.println("Premier nœud du réseau initialisé.");
        }

        scheduledTask.startScheduledTask();



        // Garder le serveur en marche
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Arrêt du serveur gRPC...");
            server.shutdown();
        }));

        server.awaitTermination();
    }
}
