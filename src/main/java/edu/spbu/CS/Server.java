package edu.spbu.CS;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Server implements Runnable {

    public Socket socket; //клиентский сокет
    private DataInputStream instream;//входной поток
    private DataOutputStream outstream;//выходной поток
    private String filename;//имя отправляемого файла

    Server(Socket sc) throws IOException {
        socket=sc;
        outstream=new DataOutputStream(sc.getOutputStream());
        instream=new DataInputStream(sc.getInputStream());
        filename="";
    }

    @Override
    public void run() {
        try {
            BufferedReader bread=new BufferedReader(new InputStreamReader(instream));
            String[] Response=bread.readLine().split(" ");
            System.out.println(Arrays.toString(Response));
            if(Response[0].equals("GET"))
            {
                String buf=Response[1].substring(1);
                System.out.println(buf);
                filename=buf;
                sendMessage();
            }else
            {
                System.out.println("WrongResponse");
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() throws IOException{
        File file=new File(filename);
        if(file.exists())
        {
            FileReader rdr=new FileReader(file);
            BufferedReader bread=new BufferedReader(rdr);
            String content,str;
            StringBuilder resBuilder = new StringBuilder();
            str=bread.readLine();
            while(str!=null)
            {
                resBuilder.append(str);
                str=bread.readLine();
            }
            rdr.close();
            content=resBuilder.toString();
            String message="HTTP/1.1 200 OK\r\n" +
                    "Server: Kakoi-to server\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Connection: close\r\n\r\n" + content;
            outstream.write(message.getBytes());
            outstream.flush();

        }else
        {
            outstream.write("<html><h2>404</h2></html>".getBytes());
            outstream.flush();
        }
        outstream.close();
    }
}