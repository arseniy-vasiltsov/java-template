package edu.spbu.CS;

import java.io.*;
import java.net.*;

public class Client implements Runnable {
    public Socket socket;//сокет
    private String response;//запрос
    private DataOutputStream outstream;//выходной поток
    private DataInputStream instream;//входной поток


    Client(String respondingfile,String servername,int port) throws IOException {
        socket= new Socket(servername,port);
        response="GET /"+respondingfile+" HTTP/1.1\r\nHost: " + servername +"\r\n\r\n";
        outstream=new DataOutputStream(socket.getOutputStream());
        instream=new DataInputStream(socket.getInputStream());
    }
    @Override
    public void run() {
        try {
            sendResponse();
            Thread.sleep(1000);
            getMessage();
        }catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void sendResponse() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outstream));
        out.write(response);
        System.out.println("Response was sent");
        out.newLine();
        out.flush();
    }

    public void getMessage()throws IOException{
        BufferedReader bread=new BufferedReader(new InputStreamReader(instream));
        String Message=bread.readLine();
        while(Message!=null)
        {
            System.out.println(Message);
            Message=bread.readLine();
        }

    }
}
