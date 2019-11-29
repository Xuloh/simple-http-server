package fr.insa.http.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A simple wrapper class for a map holding http headers that ensures validity of the headers
 */
public class HTTPHeaders {
    private Map<String, String> headers;

    public HTTPHeaders() {
        this.headers = new HashMap<>();
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

    public void clear() {
        this.headers.clear();
    }

    @Override
    public String toString() {
        return "HTTPHeaders{" + "headers=" + headers + '}';
    }
}
