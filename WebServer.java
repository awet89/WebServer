/*
 Awet Tesfamariam
 */

import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServer {

    public static void main(String argv[]) throws Exception {
        //set the port number
        int port = 45687;
        //Establish the listen socket.
        ServerSocket listenSocket = new ServerSocket(port);
        //Process HTTP service requests in an infinite loop
        while (true) {
            //Listen for a TCP connection request
            Socket TCPcon = listenSocket.accept();

            //Construct an object to process the HTTP request message.
            HttpRequest request = new HttpRequest(TCPcon);
            // Create a new thread to process the request.
            Thread thread = new Thread(request);
            // Start the thread.
            thread.start();
        }
    }
}

final class HttpRequest implements Runnable {

    final static String CRLF = "\r\n";
    Socket socket;

    //Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void processRequest() throws Exception {

        //Get a reference to the socket's input and output streams.
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        //Set up input stream filters.
        FilterInputStream fis;
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //Get the request line of the HTTP request message.
        String requestLine = br.readLine();
        //Display request line.
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines
        String headerline = null;
        while ((headerline = br.readLine()).length() != 0) {
            System.out.println(headerline);
        }
        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        // skip over the method, which should be "GET"
        tokens.nextToken();
        String fileName = tokens.nextToken();
        //Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;
        // Open the requested file.
        FileInputStream fis1 = null;
        boolean fileExists = true;
        try {
            fis1 = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }
        //Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        if (fileExists) {
            statusLine = "HTTP/1.0 200 Document Follows\r\n";
            contentTypeLine = "Content-type:" + contentType(fileName) + CRLF;
        } else {
            statusLine = "HTTP/1.0 404 Not Found\r\n";
            contentTypeLine = "no contents\n";
        }
        // Send the status line.
        os.writeBytes(statusLine);
        // Send the content type line.
        os.writeBytes(contentTypeLine);
        // Send a blank line to indicate the end of the header lines.
        os.writeBytes(CRLF);

        // Send the entity body.
        if (fileExists) {
            sendBytes(fis1, os);
            fis1.close();
        } else {
            File f = new File("nf.jpg");
            int num = (int) f.length();
            FileInputStream fil= new FileInputStream("nf.jpg");
            byte[] Bytes = new byte[num];
            sendBytes(fil,os);
        }
        os.close();
        br.close();
        socket.close();
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

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".gif") || fileName.endsWith(".GIF")) {
            return "image/gif";
        }
        if (fileName.endsWith(".jpg")||fileName.endsWith(".jpeg")) {
            return "image/jpg";
        }
        if (fileName.endsWith(".txt")) {
            return "txt file";
        }
        if (fileName.endsWith(".sh")) {
            return "bourne/awk";
        }

        return "application/octet-stream";
    }

}
