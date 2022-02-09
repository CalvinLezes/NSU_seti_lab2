package ru.nsu.fit.Dani;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class Handler implements Runnable{
    private final Socket client;
    private final File dir;
    private final Long start_time;
    private final MessageExchanger me;
    private final int clientId;

    private File file = null;
    private RandomAccessFile out = null;
    private Long bytes_read;
    private Long last_byte;
    private final long PERIOD = 3L;

    Handler(Socket clientConn, File save_dir, long currentTimeMillis, int id) {
        client = clientConn;
        dir = save_dir;
        start_time = currentTimeMillis;
        clientId = id;
        me = new MessageExchanger(client);
    }

    public void run() {
        Timer timer = new Timer();
        String file_name;
        Message name_message = me.readMessage();
        if(name_message.type != MessageType.NAME){
            System.out.println("Server expected file name, didn't get it, closing connection");
            disconnect();
        }else{
            me.sendMessage(new Message(MessageType.ACCEPT));
        }
        file_name = new String(name_message.getData());
        file = new File(dir,file_name);
        int id = 1;
        while(file.exists()){
            file = new File(dir, id+file_name);
            id++;
        }
        try {
            out = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Message length_message = me.readMessage();
        if(length_message.type != MessageType.LENGTH){
            System.out.println("Server expected file length, didn't get it, closing connection");
            disconnect();
        }else{
            me.sendMessage(new Message(MessageType.ACCEPT));
        }
        Long file_length = ByteBuffer.wrap(length_message.getData()).getLong();
        boolean receiving = true;
        bytes_read = 0L;
        last_byte = 0L;
        timer.scheduleAtFixedRate(new SpeedCounter(),PERIOD*1000, PERIOD*1000);
        while(receiving){
            Message inc_message = me.readMessage();
            switch (inc_message.type) {
                case DATA -> {
                    try {
                        out.write(inc_message.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bytes_read+=inc_message.getData().length;
                    last_byte+=inc_message.getData().length;
                }
                case FINISH -> {
                    timer.cancel();
                    receiving = false;
                }
                default -> {
                    timer.cancel();
                    receiving = false;
                    disconnect();
                }
            }
        }
        if(!bytes_read.equals(file_length)){
            System.out.println("Failed to receive file from Client" + clientId);
            me.sendMessage(new Message(MessageType.ERROR));
            if(file.exists()){
                file.delete();
            }
        }
        else{
            System.out.println("Server received file " + file_name + " from Client " + clientId + " successfully");
            me.sendMessage(new Message(MessageType.SUCCESS));
        }
        try {
            client.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class SpeedCounter extends TimerTask {
        public void run() {
            long time = ((System.currentTimeMillis() - start_time) / 1000L);
            long AvgSpeed = bytes_read / time;
            long InsSpeed = last_byte / PERIOD;
            last_byte = 0L;
            System.out.println("Avg speed is " + AvgSpeed + "b/s ,Ins speed is " + InsSpeed + " b/s");
        }
    }

    private void disconnect() {
        me.sendMessage(new Message(MessageType.ERROR));
        if(file.exists()){
            file.delete();
        }
        try {
            client.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
