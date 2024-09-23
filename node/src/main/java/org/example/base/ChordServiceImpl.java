package org.example.base;

import com.example.grpc.chord.*;
import com.example.grpc.chord.ChordGrpc.ChordImplBase;
import io.grpc.stub.StreamObserver;
import java.security.NoSuchAlgorithmException;
import com.example.grpc.chord.ChordProto.*;
import org.example.types.NodeHeader;

public class ChordServiceImpl extends ChordImplBase {
    private final ChordNode chordNode;

    public ChordServiceImpl(ChordNode chordNode) {
        this.chordNode = chordNode;
    }

    @Override
    public void join(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {

        responseObserver.onNext(JoinResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void stabilize(StabilizeRequest request, StreamObserver<StabilizeResponse> responseObserver) {
        try {
            chordNode.stabilize();
            responseObserver.onNext(StabilizeResponse.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onNext(StabilizeResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void notify(NotifyRequest request, StreamObserver<NotifyResponse> responseObserver) {
        NodeInfo callerInfo = request.getCaller();
        NodeHeader callerNode = new NodeHeader(callerInfo.getIp(), callerInfo.getPort(), callerInfo.getId());
        chordNode.notify(callerNode);
        responseObserver.onNext(NotifyResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPredecessor(NodeInfo request, StreamObserver<NodeInfo> responseObserver) {
        NodeHeader predecessor = chordNode.getPredecessor();
        if (predecessor != null) {
            NodeInfo nodeInfo = NodeInfo.newBuilder()
                    .setId(predecessor.getNodeId())
                    .setIp(predecessor.getIp())
                    .setPort(Integer.parseInt(predecessor.getPort()))
                    .build();
            responseObserver.onNext(nodeInfo);
        } else {
            responseObserver.onNext(NodeInfo.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void setPredecessor(NodeInfo request, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        NodeHeader predecessor = new NodeHeader(request.getIp(), request.getPort(), request.getId());
        chordNode.setPredecessor(predecessor);
        responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
    @Override
    public void getSuccessor(NodeInfo request, StreamObserver<NodeInfo> responseObserver) {
        NodeHeader successor = chordNode.getSuccessor();
        if (successor != null) {
            NodeInfo nodeInfo = NodeInfo.newBuilder()
                    .setId(successor.getNodeId())
                    .setIp(successor.getIp())
                    .setPort(Integer.parseInt(successor.getPort()))
                    .build();
            responseObserver.onNext(nodeInfo);
        } else {
            responseObserver.onNext(NodeInfo.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void setSuccessor(NodeInfo request, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        NodeHeader successor = new NodeHeader(request.getIp(), request.getPort(), request.getId());
        chordNode.setSuccessor(successor);
        responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateFingerTable(UpdateFingerTableRequest request, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        NodeInfo sNodeInfo = request.getS();
        int i = request.getI();
        this.chordNode.updateFingerTable(new NodeHeader(sNodeInfo.getIp(), sNodeInfo.getPort(), sNodeInfo.getId()), i);
        responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getNodeInfo(GetNodeInfoRequest request, StreamObserver<GetNodeInfoResponse> responseObserver) {
        NodeInfo nodeInfo = NodeInfo.newBuilder()
                .setId(chordNode.getNodeId())
                .setIp(chordNode.getIp())
                .setPort(chordNode.getPort())
                .build();
        responseObserver.onNext(GetNodeInfoResponse.newBuilder().setNode(nodeInfo).build());
        responseObserver.onCompleted();
    }

}
