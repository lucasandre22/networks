package com.utfpr.redes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class HttpRequest implements Runnable {

    private static final String CRLF = "\r\n";
    private Socket socket;

    @AllArgsConstructor
    @Setter
    @Getter
    private class RequestMessage {
        RequestResponse status;
        String contentTypeLine;
        String entityBody;
    }
    
    @Getter
    private enum RequestResponse {
        OK("HTTP/1.1 200 OK"),
        ERROR("HTTP/1.1 404 ERROR");

        String statusLine;
        RequestResponse(String statusLine) {
            this.statusLine = statusLine;
        }

        @Override
        public String toString() {
            return statusLine;
        }
    }

    public void run() {
        try {
            processRequest();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void processRequest() throws Exception {
        InputStream inputStream = socket.getInputStream();
        RequestMessage requestResponseMessage = null;

        FileInputStream fileStream = null;
        try {//(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String requestLine = null;
            String headerLine = null;
            int i = 0;
            while ((headerLine = reader.readLine()).length() != 0) {
                if(i++ == 0)
                    requestLine = headerLine;
                System.out.println(headerLine);
            }
            String fileName = getFileNameFromRequest(requestLine);
            System.out.println("filename:" + fileName);
            if(!new File(fileName).exists()) {
               throw new FileNotFoundException(fileName);
            }
            fileStream = new FileInputStream(fileName);
            requestResponseMessage = new RequestMessage(RequestResponse.OK, contentType(fileName) + CRLF, openAndGetFileContent(fileName) + CRLF);

        } catch(IOException | NoSuchElementException e) {
            requestResponseMessage = new RequestMessage(RequestResponse.ERROR, "Content-type: text/html; charset=UTF-8" + CRLF,
                    "<!DOCTYPE html><html>" +
                    "<head><title>Not Found</title></head>" +
                    "<body>Not Found</body></html>");
            fileStream = new FileInputStream("./error.html");
            e.printStackTrace();
        }
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        // Send the status line.
        outputStream.writeBytes(requestResponseMessage.getStatus().toString());
        // Send the content type line.
        outputStream.writeBytes(requestResponseMessage.getContentTypeLine());
        // Send a blank line to indicate the end of the header lines.
        outputStream.writeBytes(CRLF);
        //outputStream.writeBytes(requestResponseMessage.getEntityBody());
        sendBytes(fileStream, outputStream);
        fileStream.close();
        socket.close();
    }

    public String getFileNameFromRequest(String request) throws NoSuchElementException {
        StringTokenizer tokens = new StringTokenizer(request);
        tokens.nextToken(); // skip over the method, which should be "GET"
        String filename = null;
        try {
            filename = tokens.nextToken();
        } catch(NoSuchElementException e) {
            filename = "index.html"; //bad request
        }

        // Prepend a "." so that file request is within the current directory.
        return "." + filename;
    }

    public String openAndGetFileContent(String fileName) throws IOException {
        FileInputStream fileStream = null;
        String data = "";
        File file = new File(fileName);
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          data += myReader.nextLine();
        }
        myReader.close();
        return data;
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copy requested file into the socket's output stream.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "Content-type: text/html; charset=UTF-8";
        }
        if(fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
            return "Content-type: image/jpeg";
        }
        if(fileName.endsWith(".gif")) {
            return "Content-type: image/gif";
        }
        return "Content-type: application/octet-stream";
    }

    public String build404Error() {
        return "<HTML>" +
                "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                "<BODY>Not Found</BODY></HTML>";
    }
}
