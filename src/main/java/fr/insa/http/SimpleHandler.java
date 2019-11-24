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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@HTTPHandler
public class SimpleHandler {
    private static final Logger LOGGER = LogManager.getLogger(SimpleHandler.class);

    private String root;

    public SimpleHandler() {
        root = "./www";
    }

    @HandleMethod(HTTPMethod.GET)
    public HTTPResponse handleGet(HTTPRequest request) {
        String resource = request.getResource();

        try {
            if("/".equals(resource)) {
                HTTPResponse response = new HTTPResponse(HTTPStatus.MOVED_PERMANENTLY);
                response.getHeaders().setHeader("location", "/index.html");
                return response;
            }
            else if("/gif-gallery.html".equals(resource)) {
                byte[] data = this.readFileFromResources(this.root + resource);
                File gifDir = new File(this.root + "/gif");
                StringBuilder stringBuilder = new StringBuilder();
                Arrays.stream(Objects.requireNonNull(gifDir.list()))
                      .map(filename -> "<img src=\"/gif/" + filename + "\"/>")
                      .forEach(stringBuilder::append);
                stringBuilder.append("</div></body></html>");
                byte[] moreData = stringBuilder.toString().getBytes();
                byte[] allTheData = new byte[data.length + moreData.length];
                System.arraycopy(data, 0, allTheData, 0, data.length);
                System.arraycopy(moreData, 0, allTheData, data.length, moreData.length);

                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                response.getHeaders().setHeader("length", Integer.toString(allTheData.length));
                response.setBody(allTheData);
                return response;
            }
            else {
                byte[] data = this.readFileFromResources(this.root + resource);
                String contentType = this.getFileContentType(this.root + resource);
                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                if(contentType != null)
                    response.getHeaders().setHeader("content-type", contentType);
                response.getHeaders().setHeader("length", Integer.toString(data.length));
                response.setBody(data);
                return response;
            }
        }
        catch(NullPointerException | IOException e) {
            return this.notFound();
        }
    }

    @HandleMethod(HTTPMethod.POST)
    public HTTPResponse handlePost(HTTPRequest request) throws IOException {
        if("/gif".equals(request.getResource())) {
            UUID uuid = UUID.randomUUID();
            this.writeToFileInResources(this.root + "/gif/" + uuid.toString() + ".gif", request.getBody());
            return new HTTPResponse(HTTPStatus.OK);
        }
        return this.notFound();
    }

    private byte[] readFileFromResources(String path) throws IOException {
        File requestedFile = new File(path);
        if(!requestedFile.exists() || !requestedFile.canRead())
            throw new FileNotFoundException("File " + requestedFile + " does not exist or is not readable");
        String contentType = Files.probeContentType(requestedFile.toPath());
        return Files.readAllBytes(requestedFile.toPath());
    }

    private void writeToFileInResources(String path, byte[] data) throws IOException {
        File targetFile = new File(path);
        Files.write(targetFile.toPath(), data);
    }

    private String getFileContentType(String path) throws IOException {
        File requestedFile = new File(path);
        return Files.probeContentType(requestedFile.toPath());
    }

    private HTTPResponse notFound() {
        HTTPResponse response = new HTTPResponse(HTTPStatus.NOT_FOUND);
        response.setBody("<h1>404 Not Found</h1>");
        return response;
    }
}
