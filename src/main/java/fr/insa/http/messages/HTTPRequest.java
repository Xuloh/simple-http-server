package fr.insa.http.messages;

import fr.insa.http.enums.HTTPMethod;
import fr.insa.http.enums.HTTPVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class HTTPRequest extends HTTPMessage {
    private static final Logger LOGGER = LogManager.getLogger(HTTPRequest.class);

    private HTTPMethod method;

    private String resource;

    public HTTPRequest() {
        this(null, null, null);
    }

    public HTTPRequest(HTTPMethod method, String resource) {
        this(method, resource, HTTPVersion.HTTP1);
    }

    public HTTPRequest(HTTPMethod method, String resource, HTTPVersion version) {
        super(version);
        this.method = method;
        this.resource = resource;
    }

    public HTTPMethod getMethod() {
        return this.method;
    }

    public HTTPRequest setMethod(HTTPMethod method) {
        if(method == null)
            throw new NullPointerException("null method forbidden");
        if(method == HTTPMethod.ANY)
            throw new IllegalArgumentException("ANY method not permitted");
        this.method = method;
        return this;
    }

    public String getResource() {
        return this.resource;
    }

    public HTTPRequest setResource(String resource) {
        if(resource == null)
            throw new NullPointerException("null resource forbidden");
        if(resource.length() == 0)
            throw new IllegalArgumentException("empty resource forbidden");
        this.resource = resource;
        return this;
    }

    @Override
    public String toString() {
        return "Request{" + "method=" + method + ", version=" + version + ", resource='" + resource + '\'' + ", headers=" + headers + ", body='" + new String(
            body) + '\'' + '}';
    }

    @Override
    public void fromInputStream(InputStream in) throws IOException {
        this.clear();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = reader.readLine();
        int firstIdx = line.indexOf(' ');
        int lastIdx = line.lastIndexOf(' ');

        try {
            this.method = HTTPMethod.valueOf(line.substring(0, firstIdx));
            this.resource = line.substring(firstIdx + 1, lastIdx);
            this.version = HTTPVersion.fromString(line.substring(lastIdx + 1));

            // parse headers
            while((line = reader.readLine()) != null && line.length() > 0) {
                String[] split = line.split(":", 2);

                // ignore malformed lines
                if(split.length != 2) {
                    LOGGER.debug("Got malformed line : {}", line);
                    continue;
                }
                this.headers.setHeader(split[0], split[1]);
            }

            // parse body (if any)
            if(this.headers.hasHeader("content-length")) {
                int contentLength = Integer.parseInt(this.headers.getHeader("content-length"));
                byte[] bodyData = new byte[contentLength];
                int dataRead = in.read(bodyData, 0, contentLength);
                if(dataRead != contentLength)
                    throw new IllegalArgumentException("got content length " + dataRead + ", expected " + contentLength);
                this.setBody(bodyData);
            }
        }
        catch(IllegalArgumentException e) {
            LOGGER.error("Error parsing request", e);
        }
    }

    @Override
    public void toOutputStream(OutputStream out) throws IOException {
        if(this.method == null)
            throw new NullPointerException("method is null !");
        if(this.resource == null)
            throw new NullPointerException("resource is null !");
        if(this.version == null)
            throw new NullPointerException("version is null !");

        StringBuilder stringBuilder = new StringBuilder()
            .append(this.method)
            .append(' ')
            .append(this.resource)
            .append(' ')
            .append(this.version)
            .append("\r\n");

        for(String header : this.headers.headers())
            stringBuilder.append(header).append(':').append(this.headers.getHeader(header)).append("\r\n");
        stringBuilder.append("\r\n");

        byte[] data = stringBuilder.toString().getBytes();
        out.write(data, 0, data.length);

        if(this.body != null) {
            out.write(this.body, 0, this.body.length);
        }

        out.flush();
    }

    @Override
    protected void clear() {
        super.clear();
        this.method = null;
        this.resource = null;
    }
}
