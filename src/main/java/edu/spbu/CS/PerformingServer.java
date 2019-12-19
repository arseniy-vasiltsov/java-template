package edu.spbu.CS;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PerformingServer {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);
        while(true) {
            Socket s = ss.accept();
            Server serv = new Server(s);
            serv.run();
            s.close();
        }
    }

}