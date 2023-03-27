package com.utfpr.redes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Getter;

@Getter
public class LocalConnectionListener {
    private ServerSocket listenerSocket;
    private String port;

    public LocalConnectionListener(int port) throws IOException {
        listenerSocket = new ServerSocket(port);
    }
    
    public Socket listenForNewConnection() throws IOException {
        Socket socket = listenerSocket.accept();
        return socket;
    }
}
