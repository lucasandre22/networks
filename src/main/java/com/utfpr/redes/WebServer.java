package com.utfpr.redes;

import java.io.IOException;
import java.net.Socket;

public class WebServer {

    public static void main(String[] args) {
        int port = 7771;
        LocalConnectionListener connection = null;
        try {
            connection = new LocalConnectionListener(port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        while(true) {
            try {
                Socket socket = connection.listenForNewConnection();
                new Thread(new HttpRequest(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
