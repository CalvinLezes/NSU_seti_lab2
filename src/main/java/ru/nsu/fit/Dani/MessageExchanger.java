package ru.nsu.fit.Dani;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MessageExchanger {
    InputStream is;
    OutputStream os;

    MessageExchanger(Socket socket){
        try {
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean sendMessage(Message message) {
        try {
            ByteBuffer byteBuffer;
            switch (message.type) {
                case NAME, LENGTH, DATA -> {
                    byteBuffer = ByteBuffer.allocate(Byte.BYTES + message.getData().length);
                    byteBuffer.put((byte)message.type.ordinal()).put(message.getData());
                }
                default -> {
                    byteBuffer = ByteBuffer.allocate(Byte.BYTES);
                    byteBuffer.put((byte)message.type.ordinal());
                }
            }
            byte[] send_message = byteBuffer.array();
            byte[] send_mes_length = ByteBuffer.allocate(4).putInt(send_message.length).array();
            os.write(send_mes_length);
            os.write(send_message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Message readMessage() {
        try {
            byte[] size = is.readNBytes(Integer.BYTES);
            byte[] read_message = is.readNBytes(ByteBuffer.wrap(size).getInt());
            ByteBuffer buffer = ByteBuffer.wrap(read_message);
            MessageType type = MessageType.values()[(int)buffer.get()];
            switch (type){
                case NAME, LENGTH, DATA -> {
                    byte[] data_array = new byte[buffer.remaining()];
                    buffer.get(data_array);
                    return new Message(type, data_array);
                }
                default -> {
                    return new Message(type);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return new Message(MessageType.ERROR);
        }
    }
}
