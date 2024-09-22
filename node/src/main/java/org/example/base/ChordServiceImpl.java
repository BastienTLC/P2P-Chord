package org.example.base;

import com.example.grpc.chord.ChordGrpc;
import com.example.grpc.chord.ChordProto;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ChordServiceImpl extends ChordGrpc.ChordImplBase {
    private final ChordNode localNode;

    public ChordServiceImpl(ChordNode localNode) {
        this.localNode = localNode;
    }

    @Override
    public void join(ChordProto.JoinRequest request, StreamObserver<ChordProto.JoinResponse> responseObserver) {
        try {
            ChordNode n0 = new ChordNode(request.getNewNode().getIp(), request.getNewNode().getPort());
            localNode.join(n0);
            ChordProto.JoinResponse response = ChordProto.JoinResponse.newBuilder()
                    .setSuccess(true)
                    .setSuccessor(ChordProto.NodeInfo.newBuilder()
                            .setIp(localNode.getSuccessor().getIp())
                            .setPort(localNode.getSuccessor().getPort())
                            .setId(localNode.getSuccessor().getNodeId())
                            .build())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void successor(ChordProto.SuccessorRequest request, StreamObserver<ChordProto.SuccessorResponse> responseObserver) {
        try {
            ChordNode successor = localNode.findSuccessor(request.getKeyId());
            ChordProto.SuccessorResponse response = ChordProto.SuccessorResponse.newBuilder()
                    .setNode(ChordProto.NodeInfo.newBuilder()
                            .setIp(successor.getIp())
                            .setPort(successor.getPort())
                            .setId(successor.getNodeId())
                            .build())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stabilize(ChordProto.StabilizeRequest request, StreamObserver<ChordProto.StabilizeResponse> responseObserver) {
        try {
            localNode.stabilize();
            ChordProto.StabilizeResponse response = ChordProto.StabilizeResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notify(ChordProto.NotifyRequest request, StreamObserver<ChordProto.NotifyResponse> responseObserver) {
        try {
            ChordNode nx = localNode.findPredecessor(request.getCaller().getId());
            localNode.notify(nx);
            responseObserver.onNext(ChordProto.NotifyResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void leave(ChordProto.LeaveRequest request, StreamObserver<ChordProto.LeaveResponse> responseObserver) {
        try {
            localNode.leave(request.getLeavingNode().getId());
            ChordProto.LeaveResponse response = ChordProto.LeaveResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void ping(ChordProto.PingRequest request, StreamObserver<ChordProto.PingResponse> responseObserver) {
        try {
            boolean result = localNode.ping(request.getNode().getId());
            ChordProto.PingResponse response = ChordProto.PingResponse.newBuilder()
                    .setAlive(result)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

