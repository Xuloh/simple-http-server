package fr.insa.http;

import fr.insa.http.annotations.HTTPHandler;
import fr.insa.http.annotations.HandleMethod;
import fr.insa.http.enums.HTTPMethod;
import fr.insa.http.enums.HTTPStatus;
import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ResponseCache;
import java.net.URISyntaxException;
import java.nio.file.Files;

@HTTPHandler
public class SimpleHandler {
    private static final Logger LOGGER = LogManager.getLogger(SimpleHandler.class);

    private String root;

    public SimpleHandler() {
        root = "/www";
    }

    @HandleMethod(HTTPMethod.GET)
    public HTTPResponse handleGet(HTTPRequest request) {
        String resource = request.getResource();

        if("/".equals(resource)) {
            HTTPResponse response = new HTTPResponse(HTTPStatus.MOVED_PERMANENTLY);
            response.getHeaders().setHeader("location", "/index.html");
            return response;
        }
        else {
            try {
                File requestedFile = new File(this.getClass().getResource(this.root + resource).toURI());
                if(!requestedFile.exists() || !requestedFile.canRead())
                    throw new FileNotFoundException("File " + requestedFile + " does not exist or is not readable");
                String contentType = Files.probeContentType(requestedFile.toPath());
                byte[] data = Files.readAllBytes(requestedFile.toPath());

                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                if(contentType != null)
                    response.getHeaders().setHeader("content-type", contentType);
                response.getHeaders().setHeader("length", Integer.toString(data.length));
                response.setBody(data);
                return response;
            }
            catch(URISyntaxException | IOException e) {
                HTTPResponse response = new HTTPResponse(HTTPStatus.NOT_FOUND);
                response.setBody("<h1>404 Not Found</h1>");
                return response;
            }
        }
    }
}
