package fr.insa.http.messages;

import fr.insa.http.enums.HTTPVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class HTTPMessage {
    protected HTTPVersion version;

    protected Map<String, String> headers;

    protected byte[] body;

    protected HTTPMessage(HTTPVersion version) {
        this.version = version;
        this.headers = new HashMap<>();
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

    public void setHeader(String header, String value) {
        if(header == null)
            throw new NullPointerException("null header forbidden");
        if(value == null)
            this.headers.remove(header);
        else {
            header = header.toLowerCase();
            value = value.stripLeading();
            this.headers.put(header, value);
        }
    }

    public boolean hasHeader(String header) {
        if(header == null)
            throw new NullPointerException("null header forbidden");
        return this.headers.containsKey(header);
    }

    public String getHeader(String header) {
        if(header == null)
            throw new NullPointerException("null header forbidden");
        if(!this.headers.containsKey(header))
            throw new NoSuchElementException("no value for header " + header);
        return this.headers.get(header);
    }

    public Set<String> headers() {
        return Collections.unmodifiableSet(this.headers.keySet());
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
