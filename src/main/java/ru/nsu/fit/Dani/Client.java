package ru.nsu.fit.Dani;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client {
    private final File uploadFile;
    private Socket socket;

    public Client(String address, Integer port, String filePath) {
        uploadFile = new File(filePath);
        try {
            socket = new Socket(address,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        MessageExchanger me = new MessageExchanger(socket);
        me.sendMessage(new Message(MessageType.NAME,uploadFile.getName().getBytes()));
        Message feedback = me.readMessage();
        if(feedback.type.equals(MessageType.ERROR)){
            System.out.println("Delivering file name failed");
            return;
        }
        me.sendMessage(new Message(MessageType.LENGTH, ByteBuffer.allocate(Long.BYTES).putLong(uploadFile.length()).array()));
        feedback = me.readMessage();
        if(feedback.type.equals(MessageType.ERROR)){
            System.out.println("Delivering file length failed");
            return;
        }
        byte[] buffer = new byte[32000];
        RandomAccessFile rand = null;
        try {
            rand = new RandomAccessFile(uploadFile, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            int bytesRead = 0;
            try {
                bytesRead = rand.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!(bytesRead>0)) break;
            byte[] data = new byte[bytesRead];
            System.arraycopy(buffer,0,data, 0, bytesRead);
            me.sendMessage(new Message(MessageType.DATA, data));
        }
        me.sendMessage(new Message(MessageType.FINISH));
        feedback = me.readMessage();
        if(feedback.type.equals(MessageType.ERROR)){
            System.out.println("We are sorry, file wasn't delivered");
        }
        else{
            System.out.println("Congratulations, file was delivered successfully");
        }
        try {
            rand.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
