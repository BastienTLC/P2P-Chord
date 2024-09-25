package org.example.backend.Services;

import com.example.grpc.chord.ChordGrpc;
import com.example.grpc.chord.ChordProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.backend.Entity.Node;
import org.example.backend.Utils.Wrapper;

public class NodeServices {
    private final ChordGrpc.ChordBlockingStub blockingStub;

    public NodeServices(String ip, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
        blockingStub = ChordGrpc.newBlockingStub(channel);
    }


    public Node getChordNodeInfo() {
        com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();
        ChordProto.Node node = blockingStub.getChordNodeInfo(request);
        return Wrapper.wrapGrpcNodeToNode(node);
    }

}