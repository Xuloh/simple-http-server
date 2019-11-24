package fr.insa.http.util;

public enum Version {
    HTTP1("HTTP/1.1"),
    HTTP2("HTTP/2");

    private String str;

    Version(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public static Version fromString(String name) {
        if(name == null)
            throw new NullPointerException("Name is null");

        switch(name) {
            case "HTTP/1.1":
                return HTTP1;
            case "HTTP/2":
                return HTTP2;
            default:
                throw new IllegalArgumentException("Not a valid name for HTTPVersion : " + name);
        }
    }
}
