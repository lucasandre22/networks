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
            Socket socket = null;
            try {
                socket = connection.listenForNewConnection();
                new Thread(new HttpRequestProcessor(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
