package ru.nsu.fit.Dani;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService pool;

    public Server(Integer port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool = Executors.newCachedThreadPool();
    }

    public void run(){
        System.out.println("Server running");
        File save_dir = new File("upload");
        if(!save_dir.exists()){
            save_dir.mkdir();
        }
        try {
            int id = 0;
            while(!serverSocket.isClosed()){
                Socket clientConn = serverSocket.accept();
                System.out.println("Client "+ id + " connected");
                Handler serverTask = new Handler(clientConn,save_dir,System.currentTimeMillis(), id);
                pool.submit(serverTask);
                id++;
            }
            pool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }

    }
}
