package fr.insa.http;

import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;

/**
 * A class that implements this interface can handle HTTPRequests and return HTTPResponses
 */
@FunctionalInterface
public interface RequestHandler {
    HTTPResponse handleRequest(HTTPRequest request);
}
