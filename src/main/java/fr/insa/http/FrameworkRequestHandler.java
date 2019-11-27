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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrameworkRequestHandler implements RequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(FrameworkRequestHandler.class);
    
    private Object handlerInstance;

    private List<Method> handlerMethods;

    private Map<HTTPMethod, Method> defaultMethods;

    private HTTPHeaders defaultHeaders;
    
    public FrameworkRequestHandler() {
        Reflections reflections = new Reflections("fr.insa.http");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(HTTPHandler.class);
        this.defaultMethods = new EnumMap<>(HTTPMethod.class);
        this.defaultHeaders = new HTTPHeaders();
        
        if(annotated.size() == 0)
            LOGGER.warn("No class annotated with @HTTPHandler could be found");
        else {
            if(annotated.size() > 1)
                LOGGER.warn("Only one class should be annotated with @HTTPHandler, using class");

            Class<?> handlerClass = annotated.iterator().next();
            LOGGER.debug("Using handler class : {}", handlerClass.getName());

            try {
                Constructor<?> constructor = handlerClass.getConstructor();
                this.handlerInstance = constructor.newInstance();
                Arrays
                    .stream(handlerClass.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 1)
                    .filter(method -> method.getParameterTypes()[0] == HTTPRequest.class)
                    .filter(method -> method.getReturnType() == HTTPResponse.class)
                    .forEach(method -> {
                        HandleMethod handleMethod = method.getAnnotation(HandleMethod.class);
                        if(handleMethod != null) {
                            if(this.defaultMethods.containsKey(handleMethod.value()))
                                LOGGER.warn("Only one method is allowed per HTTP method, ignoring {}", method.getName());
                            else {
                                LOGGER.debug("Handler method for {} requests : {}", handleMethod.value(), method.getName());
                                this.defaultMethods.put(handleMethod.value(), method);
                            }
                        }
                    });
            }
            catch(InstantiationException e) {
                LOGGER.error("Can't instantiate abstract class {}", handlerClass.getName(), e);
            }
            catch(InvocationTargetException e) {
                LOGGER.error("Default constructor in class {} threw an exception", handlerClass.getName(), e);
            }
            catch(NoSuchMethodException e) {
                LOGGER.error("Class {} must have a default public constructor", handlerClass.getName(), e);
            }
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
        Method handlerMethod = this.defaultMethods.get(request.getMethod());
        HTTPResponse response;
        if(handlerMethod == null)
            response = this.noHandlerMethod(request);
        else {
            try {
                response = (HTTPResponse)handlerMethod.invoke(this.handlerInstance, request);
            }
            catch(IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("An error occurred while invoking handler method", e);
                response = this.errorResponse(e);
            }
        }

        for(String header : this.defaultHeaders.headers()) {
            if(!response.getHeaders().hasHeader(header))
                response.getHeaders().setHeader(header, this.defaultHeaders.getHeader(header));
        }

        return response;
    }

    private HTTPResponse noHandlerMethod(HTTPRequest request) {
        LOGGER.warn("No method registered to handle {} requests", request.getMethod());
        return new HTTPResponse(HTTPStatus.NOT_IMPLEMENTED);
    }

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
