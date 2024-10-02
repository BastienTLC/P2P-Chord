package org.example.backend.Services;

import com.example.grpc.chord.ChordGrpc;
import com.example.grpc.chord.ChordProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.backend.Entity.Message;
import org.example.backend.Entity.Node;
import org.example.backend.Utils.Wrapper;

import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStreamReader;

public class NodeServices {
    private final ChordGrpc.ChordBlockingStub blockingStub;

    public NodeServices(String ip, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
        blockingStub = ChordGrpc.newBlockingStub(channel);
    }


    public boolean storeMessageInChord(String key, Message message) {
        ChordProto.Message messageStore = Wrapper.wrapMessageToGrpcMessage(message);
        ChordProto.StoreMessageRequest request = ChordProto.StoreMessageRequest.newBuilder()
                .setKey(key)
                .setMessage(messageStore)
                .build();
        ChordProto.StoreMessageResponse response = blockingStub.storeMessageInChord(request);
        return response.getSuccess();
    }

    public Message retrieveMessageFromChord(String key) {
        ChordProto.RetrieveMessageRequest request = ChordProto.RetrieveMessageRequest.newBuilder()
                .setKey(key)
                .build();
        ChordProto.Message message = blockingStub.retrieveMessageFromChord(request).getMessage();
        return Wrapper.wrapGrpcMessageToMessage(message);
    }

    public Node getChordNodeInfo() {
        com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();
        ChordProto.Node node = blockingStub.getChordNodeInfo(request);
        return Wrapper.wrapGrpcNodeToNode(node);
    }

    public boolean runNode(String host, int port, String joinIp, int joinPort) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-jar", "chordNode-1.0-SNAPSHOT.jar",
                    "-host", host,
                    "-port", String.valueOf(port),
                    "-joinIp", joinIp,
                    "-joinPort", String.valueOf(joinPort)
            );

            processBuilder.directory(new File("../node/target"));
            processBuilder.inheritIO();

            // Start the process
            processBuilder.start();

            // Return true immediately after starting the process
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean runNode(String host, int port) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-jar", "chordNode-1.0-SNAPSHOT.jar",
                    "-host", host,
                    "-port", String.valueOf(port)
            );

            processBuilder.directory(new File("../node/target"));
            processBuilder.inheritIO();

            processBuilder.start();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean shutdownNode(int port){
        //kill $(lsof -t -i:8080)
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "kill", "$", "lsof", "-t", "-i:"+port
            );

            processBuilder.inheritIO();

            processBuilder.start();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean stopNode() {
        com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();
        int port = getChordNodeInfo().getPort();
        boolean leave = blockingStub.leave(request).getSuccess();
        boolean shutdown = shutdownNode(port);
        return leave ;

    }

}