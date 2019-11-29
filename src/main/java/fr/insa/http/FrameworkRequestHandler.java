package fr.insa.http;

import fr.insa.http.annotations.HTTPHandler;
import fr.insa.http.annotations.HandleMethod;
import fr.insa.http.enums.HTTPMethod;
import fr.insa.http.enums.HTTPStatus;
import fr.insa.http.messages.HTTPRequest;
import fr.insa.http.messages.HTTPResponse;
import fr.insa.http.util.HTTPHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of RequestHandler. It does nothing alone.
 * However, using reflexion and annotations, it can find a class to delegate requests handling.
 * This allows for easy modification of request handling by just modifying the annotated class.
 */
public class FrameworkRequestHandler implements RequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(FrameworkRequestHandler.class);

    // the object that will handle requests
    private Object handlerInstance;

    // the methods that will be used to handle requests based on their http method
    private Map<HTTPMethod, Method> defaultMethods;

    // some default headers that will be added to responses
    private HTTPHeaders defaultHeaders;
    
    public FrameworkRequestHandler() {
        this.defaultMethods = new EnumMap<>(HTTPMethod.class);
        this.defaultHeaders = new HTTPHeaders();

        // find all classes annotated with HTTPHandler
        Reflections reflections = new Reflections("fr.insa.http");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(HTTPHandler.class);

        // print a message if no class could be found
        if(annotated.size() == 0)
            LOGGER.warn("No class annotated with @HTTPHandler could be found");
        else {
            // print a message if more that one class was found
            if(annotated.size() > 1)
                LOGGER.warn("Only one class should be annotated with @HTTPHandler, using class");

            Class<?> handlerClass = annotated.iterator().next();
            LOGGER.debug("Using handler class : {}", handlerClass.getName());

            try {
                // get the default constructor of the handler class and instantiate it
                Constructor<?> constructor = handlerClass.getConstructor();
                this.handlerInstance = constructor.newInstance();

                Arrays
                    .stream(handlerClass.getDeclaredMethods()) // get all the method of the handler class
                    .filter(method -> method.getParameterCount() == 1) // keep those that have 1 parameter
                    .filter(method -> method.getParameterTypes()[0] == HTTPRequest.class) // keep those that take an HTTPRequest as parameter
                    .filter(method -> method.getReturnType() == HTTPResponse.class) // keep those that return an HTTPResponse
                    .forEach(method -> {
                        // try to get the HandleMethod annotation from the method
                        HandleMethod handleMethod = method.getAnnotation(HandleMethod.class);

                        // ignore methods that are not annotated
                        if(handleMethod != null) {
                            // if we already have a method to handle the given http method, ignore it
                            if(this.defaultMethods.containsKey(handleMethod.value()))
                                LOGGER.warn("Only one method is allowed per HTTP method, ignoring {}", method.getName());
                            // else we can handle this http method \o/
                            else {
                                LOGGER.debug("Handler method for {} requests : {}", handleMethod.value(), method.getName());
                                this.defaultMethods.put(handleMethod.value(), method);
                            }
                        }
                    });
            }
            // if the handler class can't be instantiated (abstract class or interface)
            catch(InstantiationException e) {
                LOGGER.error("Can't instantiate abstract class {}", handlerClass.getName(), e);
            }
            // if the handler class constructor threw an exception
            catch(InvocationTargetException e) {
                LOGGER.error("Default constructor in class {} threw an exception", handlerClass.getName(), e);
            }
            // if that handler class does not have a public no-arg constructor
            catch(NoSuchMethodException e) {
                LOGGER.error("Class {} must have a default public constructor", handlerClass.getName(), e);
            }
            // if instantiation fails for any other reason (should not happen too often)
            catch(IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public HTTPHeaders defaultHeaders() {
        return this.defaultHeaders;
    }

    @Override
    public HTTPResponse handleRequest(HTTPRequest request) {
        // find which of our handler methods can handle this request
        Method handlerMethod = this.defaultMethods.get(request.getMethod());
        HTTPResponse response;

        // if we don't have any method for this request
        if(handlerMethod == null)
            response = this.noHandlerMethod(request);
        else {
            // try to call the handler method
            try {
                response = (HTTPResponse)handlerMethod.invoke(this.handlerInstance, request);
            }
            // if we can't call the method or it throws an uncaught exception
            catch(IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("An error occurred while invoking handler method", e);
                response = this.errorResponse(e);
            }
        }

        // add the default headers to the response
        for(String header : this.defaultHeaders.headers()) {
            if(!response.getHeaders().hasHeader(header))
                response.getHeaders().setHeader(header, this.defaultHeaders.getHeader(header));
        }

        return response;
    }

    // helper method to created a NOT_IMPLEMENTED response
    private HTTPResponse noHandlerMethod(HTTPRequest request) {
        LOGGER.warn("No method registered to handle {} requests", request.getMethod());
        return new HTTPResponse(HTTPStatus.NOT_IMPLEMENTED);
    }

    // helper method to create a INTERNAL_SERVER_ERROR response
    // will show the exception's stack trace on the page
    private HTTPResponse errorResponse(Exception e) {
        HTTPResponse response = new HTTPResponse(HTTPStatus.INTERNAL_SERVER_ERROR);

        StringBuilder stringBuilder = new StringBuilder()
            .append("<h1>500 Internal Server Error :</h1>")
            .append("<h2>")
            .append(e.getMessage())
            .append("</h2>");

        stringBuilder.append("<ul>");
        for(StackTraceElement element : e.getStackTrace()) {
            stringBuilder
                .append("<li>")
                .append(element.toString())
                .append("</li>");
        }
        stringBuilder.append("</ul>");

        response.setBody(stringBuilder.toString());

        return response;
    }
}
