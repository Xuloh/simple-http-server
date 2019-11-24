package fr.insa.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    public static void main(String[] args) {
        int port = 8080;
        LOGGER.info("Starting server on port {}", port);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            FrameworkRequestHandler requestHandler = new FrameworkRequestHandler();
            while(true) {
                Socket socket = serverSocket.accept();
                new WorkerThread(socket, requestHandler).start();
            }
        }
        catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
