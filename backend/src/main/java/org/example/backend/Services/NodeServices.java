package org.example.backend.Services;

import com.example.grpc.application.ApplicationGrpc;
import com.example.grpc.application.ApplicationProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import org.example.backend.Entity.Node;
import com.example.grpc.application.*;
import org.example.backend.Utils.Wrapper;

public class NodeServices {
    private final ApplicationGrpc.ApplicationBlockingStub blockingStub;

    public NodeServices(String ip, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
        blockingStub = ApplicationGrpc.newBlockingStub(channel);
    }


    public Node getChordNodeInfo() {
        com.google.protobuf.Empty request = com.google.protobuf.Empty.newBuilder().build();
        ApplicationProto.Node node = blockingStub.getChordNodeInfo(request);
        return Wrapper.wrapGrpcNodeToNode(node);
    }

}