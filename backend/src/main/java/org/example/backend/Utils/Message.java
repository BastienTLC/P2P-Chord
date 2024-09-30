package org.example.backend.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {


    public static List<org.example.backend.Entity.Message> createMessages(int quantity, int dataSize) {
        List<org.example.backend.Entity.Message> messages = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            org.example.backend.Entity.Message message = new org.example.backend.Entity.Message(
                    String.valueOf(i),
                    //random timestamp
                    System.currentTimeMillis() + (long)(Math.random() * 1000000),
                    createFixedLengthAuthorName(10),
                    createFixedLengthTopic(10),
                    createFixedLengthContent(100),
                    createFixedDataSize(dataSize)
            );
            messages.add(message);
        }
        return messages;
    }

    private static String createFixedLengthAuthorName(int length) {
        return generateFixedString(length, 'A');
    }

    private static String createFixedLengthContent(int length) {
        return generateFixedString(length, 'C');
    }

    private static String createFixedLengthTopic(int length) {
        return generateFixedString(length, 'T');
    }

    private static byte[] createFixedDataSize(int size) {
        byte[] data = new byte[size];
        Arrays.fill(data, (byte) 0);
        return data;
    }

    private static String generateFixedString(int length, char c) {
        char[] chars = new char[length];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
