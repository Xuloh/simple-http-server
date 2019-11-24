package fr.insa.http.messages;

import fr.insa.http.enums.HTTPStatus;
import fr.insa.http.enums.HTTPVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class HTTPResponse extends HTTPMessage {
    private static final Logger LOGGER = LogManager.getLogger(HTTPResponse.class);

    private HTTPStatus status;

    public HTTPResponse() {
        this(null, null);
    }

    public HTTPResponse(HTTPStatus status) {
        this(status, null);
    }

    public HTTPResponse(HTTPStatus status, HTTPVersion version) {
        super(version);
        this.status = status;
    }

    public HTTPStatus getStatus() {
        return this.status;
    }

    public HTTPResponse setStatus(HTTPStatus status) {
        if(status == null)
            throw new NullPointerException("null status forbidden");
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "Response{" + "version=" + version + ", status=" + status + ", headers=" + headers + ", body='" + body + '\'' + '}';
    }

    @Override
    public void fromInputStream(InputStream in) throws IOException {
        this.clear();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = reader.readLine();
        String[] split = line.split(" ", 2);

        try {
            this.version = HTTPVersion.fromString(split[0]);
            this.status = HTTPStatus.fromString(split[1]);

            // parse headers
            while((line = reader.readLine()) != null && line.length() > 0) {
                split = line.split(":", 2);

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
        if(this.version == null)
            throw new NullPointerException("version is null !");
        if(this.status == null)
            throw new NullPointerException("status is null !");

        StringBuilder stringBuilder = new StringBuilder()
            .append(this.version)
            .append(' ')
            .append(this.status)
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
        this.status = null;
    }
}
