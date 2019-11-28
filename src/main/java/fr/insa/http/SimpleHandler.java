package fr.insa.http;

import fr.insa.http.annotations.HTTPHandler;
import fr.insa.http.annotations.HandleMethod;
import fr.insa.http.enums.HTTPMethod;
import fr.insa.http.enums.HTTPStatus;
import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;
import fr.insa.http.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

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
                byte[] data = this.readFile(this.root + resource);
                File gifDir = new File(this.root + "/gif");
                StringBuilder stringBuilder = new StringBuilder();
                Arrays.stream(Objects.requireNonNull(gifDir.list()))
                      .map(filename -> "<img src=\"/gif/" + filename + "\"/>")
                      .forEach(stringBuilder::append);
                stringBuilder.append("</div></body></html>");
                byte[] moreData = stringBuilder.toString().getBytes();
                byte[] allTheData = Util.concatenateArrays(data, moreData);

                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                response.getHeaders().setHeader("content-length", Integer.toString(allTheData.length));
                response.setBody(allTheData);
                return response;
            }
            else {
                byte[] data = this.readFile(this.root + resource);
                String contentType = this.getFileContentType(this.root + resource);
                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                if(contentType != null)
                    response.getHeaders().setHeader("content-type", contentType);
                response.getHeaders().setHeader("content-length", Integer.toString(data.length));
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
            Base64.Decoder base64 = Base64.getDecoder();
            byte[] data = base64.decode(request.getBody());
            this.writeToFile(this.root + "/gif/" + uuid.toString() + ".gif", data);
            return new HTTPResponse(HTTPStatus.OK);
        }
        return this.notFound();
    }

    @HandleMethod(HTTPMethod.HEAD)
    public HTTPResponse handleHead(HTTPRequest request) {
        String resource = request.getResource();

        try {
            if("/".equals(resource)) {
                HTTPResponse response = new HTTPResponse(HTTPStatus.MOVED_PERMANENTLY);
                response.getHeaders().setHeader("location", "/index.html");
                return response;
            }
            else if("/gif-gallery.html".equals(resource)) {
                byte[] data = this.readFile(this.root + resource);
                File gifDir = new File(this.root + "/gif");
                StringBuilder stringBuilder = new StringBuilder();
                Arrays.stream(Objects.requireNonNull(gifDir.list()))
                        .map(filename -> "<img src=\"/gif/" + filename + "\"/>")
                        .forEach(stringBuilder::append);
                stringBuilder.append("</div></body></html>");
                byte[] moreData = stringBuilder.toString().getBytes();
                byte[] allTheData = Util.concatenateArrays(data, moreData);

                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                response.getHeaders().setHeader("content-length", Integer.toString(allTheData.length));
                return response;
            }
            else {
                byte[] data = this.readFile(this.root + resource);
                String contentType = this.getFileContentType(this.root + resource);
                HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
                if(contentType != null)
                    response.getHeaders().setHeader("content-type", contentType);
                response.getHeaders().setHeader("content-length", Integer.toString(data.length));
                return response;
            }
        }
        catch(NullPointerException | IOException e) {
            return this.notFound();
        }
    }

    @HandleMethod(HTTPMethod.PUT)
    public HTTPResponse handlePut(HTTPRequest request) {
        String resource = request.getResource();

        try {
            if("/".equals(resource)) {
                HTTPResponse response = new HTTPResponse(HTTPStatus.MOVED_PERMANENTLY);
                response.getHeaders().setHeader("location", "/index.html");
                return response;
            }
            else if("/gif-gallery.html".equals(resource)) {
                return new HTTPResponse(HTTPStatus.METHOD_NOT_ALLOWED);
            }
            else {
                File file = new File(this.root + resource);
                if(!file.exists()){
                    return new HTTPResponse(HTTPStatus.NOT_FOUND);
                }
                String contentType = this.getFileContentType(this.root + resource);
                if(contentType != null && !contentType.equals(request.getHeaders().getHeader("content-type"))) {
                    return new HTTPResponse(HTTPStatus.BAD_REQUEST);
                }
                byte[] data = request.getBody();
                this.writeToFile(this.root + resource, data);
                return new HTTPResponse(HTTPStatus.OK);
            }
        }
        catch(NullPointerException | IOException e) {
            return this.notFound();
        }
    }

    private byte[] readFile(String path) throws IOException {
        File requestedFile = new File(path);
        if(!requestedFile.exists() || !requestedFile.canRead())
            throw new FileNotFoundException("File " + requestedFile + " does not exist or is not readable");
        String contentType = Files.probeContentType(requestedFile.toPath());
        return Files.readAllBytes(requestedFile.toPath());
    }

    private void writeToFile(String path, byte[] data) throws IOException {
        this.writeToFile(path, data, false);
    }

    private void writeToFile(String path, byte[] data, boolean gzip) throws IOException {
        byte[] fileData = new byte[0];
        if(gzip) {
            GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int count = gzipIn.read(buffer, 0, buffer.length);
            do {
                fileData = Util.concatenateArrays(fileData, 0, fileData.length, buffer, 0, count);
                count = gzipIn.read(buffer, 0, buffer.length);
            } while(count != -1);
        }
        else
            fileData = data;
        File targetFile = new File(path);
        Files.write(targetFile.toPath(), fileData);
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
