package fr.insa.http;

import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;

@FunctionalInterface
public interface RequestHandler {
    HTTPResponse handleRequest(HTTPRequest request);
}
