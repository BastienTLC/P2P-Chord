package org.example.base;

import com.example.grpc.chord.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import com.example.grpc.chord.ChordProto.*;
import org.example.types.NodeHeader;

public class ChordClient {
    private final ChordGrpc.ChordBlockingStub blockingStub;
    private final ManagedChannel channel;

    public ChordClient(String ip, int port) {
        channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
                .build();
        blockingStub = ChordGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        channel.shutdown();
    }

    public NodeHeader findSuccessor(String keyId) {
        NodeInfo request = NodeInfo.newBuilder().setId(keyId).build();

        try {
            NodeInfo nodeInfo = blockingStub.findSuccessor(request);
            return new NodeHeader(nodeInfo.getIp(), nodeInfo.getPort(), nodeInfo.getId());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public NodeHeader getPredecessor(NodeHeader node) {
        NodeInfo request = NodeInfo.newBuilder()
                .setId(node.getNodeId())
                .setIp(node.getIp())
                .setPort(Integer.parseInt(node.getPort()))
                .build();

        try {
            NodeInfo nodeInfo = blockingStub.getPredecessor(request);
            return new NodeHeader(nodeInfo.getIp(), nodeInfo.getPort(), nodeInfo.getId());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void setPredecessor(ChordNode node) {
        NodeInfo nodeInfo = NodeInfo.newBuilder()
                .setId(node.getNodeId())
                .setIp(node.getIp())
                .setPort(node.getPort())
                .build();
        try {
            blockingStub.setPredecessor(nodeInfo);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    public NodeHeader getSuccessor() {
        NodeInfo request = NodeInfo.newBuilder().build();

        try {
            NodeInfo nodeInfo = blockingStub.getSuccessor(request);
            return new NodeHeader(nodeInfo.getIp(), nodeInfo.getPort(), nodeInfo.getId());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setSuccessor(ChordNode node) {
        NodeInfo nodeInfo = NodeInfo.newBuilder()
                .setId(node.getNodeId())
                .setIp(node.getIp())
                .setPort(node.getPort())
                .build();
        try {
            blockingStub.setSuccessor(nodeInfo);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }



    public void notify(ChordNode node) {
        NodeInfo nodeInfo = NodeInfo.newBuilder()
                .setId(node.getNodeId())
                .setIp(node.getIp())
                .setPort(node.getPort())
                .build();

        NotifyRequest request = NotifyRequest.newBuilder()
                .setCaller(nodeInfo)
                .build();

        try {
            blockingStub.notify(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    public void updateFingerTable(NodeHeader s, int i) {
        NodeInfo sNodeInfo = NodeInfo.newBuilder()
                .setId(s.getNodeId())
                .setIp(s.getIp())
                .setPort(Integer.parseInt(s.getPort()))
                .build();

        UpdateFingerTableRequest request = UpdateFingerTableRequest.newBuilder()
                .setS(sNodeInfo)
                .setI(i)
                .build();

        try {
            blockingStub.updateFingerTable(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    public NodeHeader getNodeInfo() {
        GetNodeInfoRequest request = GetNodeInfoRequest.newBuilder().build();

        try {
            GetNodeInfoResponse response = blockingStub.getNodeInfo(request);
            NodeInfo nodeInfo = response.getNode();
            return new NodeHeader(nodeInfo.getIp(), nodeInfo.getPort(), nodeInfo.getId());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Implement other client methods as needed...
}
