package fr.insa.http;

import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class WorkerThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(WorkerThread.class);

    private static int workerCount = 0;

    private Socket socket;

    private RequestHandler requestHandler;

    public WorkerThread(Socket socket, RequestHandler requestHandler) {
        super("WorkerThread-" + workerCount++);
        this.socket = socket;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        try {
            HTTPRequest request = new HTTPRequest();
            request.fromInputStream(this.socket.getInputStream());
            LOGGER.info("Received request : {} {}", request.getMethod(), request.getResource());

            HTTPResponse response = this.requestHandler.handleRequest(request);
            LOGGER.info("Sending response : {}", response.getStatus());
            response.toOutputStream(this.socket.getOutputStream());

            this.socket.close();
        }
        catch(IOException e) {
            LOGGER.error("An error occurred while handling request", e);
        }
    }
}
