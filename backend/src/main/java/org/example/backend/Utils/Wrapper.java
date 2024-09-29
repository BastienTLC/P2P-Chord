package org.example.backend.Utils;

import com.example.grpc.chord.ChordProto;
import org.example.backend.Entity.FingerTable;
import org.example.backend.Entity.Message;
import org.example.backend.Entity.Node;
import org.example.backend.Entity.NodeHeader;

import java.util.stream.Collectors;

public class Wrapper {

    public static Node wrapGrpcNodeToNode(ChordProto.Node grpcNode) {
        return new Node(
                grpcNode.getIp(),
                grpcNode.getPort(),
                grpcNode.getId(),
                wrapGrpcNodeInfoToNodeHeader(grpcNode.getPredecessor()),
                wrapGrpcNodeInfoToNodeHeader(grpcNode.getSuccessor()),
                wrapGrpcFingerTableToNodeHeaderList(grpcNode.getFingerTable()),
                grpcNode.getM(),
                wrapGrpcMessagesToMessages(grpcNode.getMessageStore())
        );
    }

    public static NodeHeader wrapGrpcNodeInfoToNodeHeader(ChordProto.NodeInfo grpcNodeInfo) {
        return new NodeHeader(
                grpcNodeInfo.getIp(),
                grpcNodeInfo.getPort(),
                grpcNodeInfo.getId()
        );
    }

    public static FingerTable wrapGrpcFingerTableToNodeHeaderList(ChordProto.FingerTable grpcFingerTable) {
        return new FingerTable(
                grpcFingerTable.getFingerList().stream()
                        .map(finger -> new NodeHeader(
                                finger.getIp(),
                                finger.getPort(),
                                finger.getId()
                        ))
                        .collect(Collectors.toList())
        );
    }

    public static Message[] wrapGrpcMessagesToMessages(ChordProto.MessageStore grpcMessages) {
        return grpcMessages.getMessagesList().stream()
                .map(message -> new Message(
                        message.getId(),
                        message.getTimestamp(),
                        message.getAuthor(),
                        message.getTopic(),
                        message.getContent(),
                        message.getData().toByteArray()
                ))
                .toArray(Message[]::new);
    }

    public static ChordProto.MessageStore wrapMessagesToGrpcMessages(Message message) {
        return ChordProto.MessageStore.newBuilder()
                .addMessages(ChordProto.Message.newBuilder()
                        .setId(message.getId())
                        .setTimestamp(message.getTimestamp())
                        .setAuthor(message.getAuthor())
                        .setTopic(message.getTopic())
                        .setContent(message.getContent())
                        .setData(com.google.protobuf.ByteString.copyFrom(message.getData()))
                        .build())
                .build();
    }

    public static Message wrapGrpcMessageToMessage(ChordProto.Message grpcMessage) {
        return new Message(
                grpcMessage.getId(),
                grpcMessage.getTimestamp(),
                grpcMessage.getAuthor(),
                grpcMessage.getTopic(),
                grpcMessage.getContent(),
                grpcMessage.getData().toByteArray()
        );
    }

    public static ChordProto.Message wrapMessageToGrpcMessage(Message message) {
        return ChordProto.Message.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setAuthor(message.getAuthor())
                .setTopic(message.getTopic())
                .setContent(message.getContent())
                .setData(com.google.protobuf.ByteString.copyFrom(message.getData()))
                .build();
    }
}
