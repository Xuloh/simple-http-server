package fr.insa.http.messages;

import fr.insa.http.enums.HTTPVersion;
import fr.insa.http.util.HTTPHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract superclass that represents an HTTPMessage
 * Subclassed by HTTPRequest and HTTPResponse
 */
public abstract class HTTPMessage {
    public static HTTPVersion defaultVersion = HTTPVersion.HTTP1;

    protected HTTPVersion version;

    protected HTTPHeaders headers;
    protected byte[] body;

    protected HTTPMessage(HTTPVersion version) {
        this.version = version == null ? defaultVersion : version;
        this.headers = new HTTPHeaders();
        this.body = null;
    }

    public abstract void fromInputStream(InputStream in) throws IOException;

    public abstract void toOutputStream(OutputStream out) throws IOException;

    public HTTPVersion getVersion() {
        return this.version;
    }

    public void setVersion(HTTPVersion version) {
        if(version == null)
            throw new NullPointerException("null version forbidden");
        this.version = version;
    }

    public HTTPHeaders getHeaders() {
        return this.headers;
    }

    public String getBodyAsString() {
        return new String(this.body);
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body.getBytes();
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    protected void clear() {
        this.version = null;
        this.headers.clear();
        this.body = null;
    }
}
