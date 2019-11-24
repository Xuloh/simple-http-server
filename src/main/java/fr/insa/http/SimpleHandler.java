package fr.insa.http;

import fr.insa.http.annotations.HTTPHandler;
import fr.insa.http.annotations.HandleMethod;
import fr.insa.http.enums.HTTPMethod;
import fr.insa.http.enums.HTTPStatus;
import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;

@HTTPHandler
public class SimpleHandler {

    @HandleMethod(HTTPMethod.GET)
    public HTTPResponse handleGet(HTTPRequest request) {
        HTTPResponse response = new HTTPResponse(HTTPStatus.OK);
        response.setBody("<h1>It works !</h1>");
        return response;
    }
}
