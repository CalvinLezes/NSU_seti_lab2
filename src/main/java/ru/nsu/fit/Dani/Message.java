package ru.nsu.fit.Dani;

public class Message {
    public MessageType type;
    private byte[] data = null;

    public Message(MessageType messageType, byte[] data_array) {
        type = messageType;
        data = data_array;
    }

    public Message(MessageType messageType) {
        type = messageType;
    }

    public byte[] getData() {
        return data;
    }
}