package fr.insa.http;

import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * A thread that can handle a single HTTP request before closing the connection
 */
public class WorkerThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(WorkerThread.class);

    private static int workerCount = 0;

    private Socket socket;

    private RequestHandler requestHandler;

    /**
     * The thread will read a request from the given socket and send the response produced by the given RequestHandler
     */
    public WorkerThread(Socket socket, RequestHandler requestHandler) throws SocketException {
        super("WorkerThread-" + workerCount++);
        this.socket = socket;
        LOGGER.trace("Socket info - SO_RCVBUF={}", this.socket.getReceiveBufferSize());
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        try {
            // create a request from the socket inputstream
            HTTPRequest request = new HTTPRequest();
            request.fromInputStream(this.socket.getInputStream());
            LOGGER.info("Received request : {} {}", request.getMethod(), request.getResource());

            // get the response from our request handler
            HTTPResponse response = this.requestHandler.handleRequest(request);
            LOGGER.info("Sending response : {}", response.getStatus());

            // write the response to the socket outputstream
            response.toOutputStream(this.socket.getOutputStream());

            // close the connection
            this.socket.close();
        }
        catch(NullPointerException e) {
            LOGGER.warn("Nothing to read", e);
        }
        catch(IOException e) {
            LOGGER.error("An error occurred while handling request", e);
        }
    }
}
