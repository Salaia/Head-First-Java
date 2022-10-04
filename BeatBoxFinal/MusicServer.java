import java.io.*;
import java.net.*;
import java.util.*;

public class MusicServer {

    ArrayList<ObjectOutputStream> clientOutputStreams;

    public static void main(String[] args) {
        new MusicServer().go(); // Даже без объявления переменной? как грубо... надеюсь, сервер не обидится.
    }// main

    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket clientSocket;

        // constructor ClientHandler
        public ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (Exception ex) {ex.printStackTrace();}
        } // constructor ClientHandler

        public void run() {
            Object o2 = null;
            Object o1 = null;
            // В обратном порядке, хотя от этого ничего не зависит, кроме моего эстетического восприятия логики
            try {
                while ((o1 = in.readObject()) != null) {
                    o2 = in.readObject();
                    System.out.println("Read two objects");
                    tellEveryone(o1,o2); // Тут мы всё же следуем логике, ага
                } // while < try < run < ClientHandler
            } catch (Exception ex) {ex.printStackTrace();}
        } // run < ClientHandler
    }// ClientHandler

    public void go() {
        clientOutputStreams = new ArrayList<ObjectOutputStream>();

        try {
            ServerSocket serverSock = new ServerSocket(4242);

            while (true) {
                Socket clientSocket = serverSock.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

                System.out.println("Got a connection");
            } // while < try < go
        } catch (Exception ex) {ex.printStackTrace();}
    } // go

    public void tellEveryone(Object one, Object two) {
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                ObjectOutputStream out = (ObjectOutputStream) it.next();
                out.writeObject(one);
                out.writeObject(two);
            } catch (Exception ex) {ex.printStackTrace();}
        } // while < tellEveryone
    }// tellEveryone
}// MusicServer
