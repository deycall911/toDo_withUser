package toDoWithFavorites;

import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.lang.RuleBookBuilder;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import static java.util.logging.Level.INFO;

public class LoggerFilter implements ClientRequestFilter, ClientResponseFilter {
    private static final Logger LOG = Logger.getLogger(LoggerFilter.class.getName());

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        LOG.log(INFO, requestToLogString(requestContext));
    }

    private String requestToLogString(ClientRequestContext requestContext) {
        StringBuilder result = new StringBuilder("\nThreadId: " + Thread.currentThread().getId()).append("\n");
        result.append("Method: ").append(requestContext.getMethod()).append("\n");
        result.append("URL: ").append(requestContext.getUri()).append("\n");
        requestContext.getHeaders().forEach((headerName, headerValues) -> {
            result.append(headerName).append(": ");
            result.append(headerValues.stream().map(Object::toString).reduce("", (v1,v2) -> v1 + ", " + v2).substring(1));
            result.append("\n");
        });
        result.append("BODY: ").append(requestContext.getEntity());
        return result.toString();
    }

    private String responseToLogString(ClientResponseContext responseContext) {
        StringBuilder result = new StringBuilder("\nThreadId: " + Thread.currentThread().getId()).append("\n");
        result.append("Status: ").append(responseContext.getStatus()).append("\n");
        responseContext.getHeaders().forEach((headerName, headerValues) -> {
            result.append(headerName).append(": ");
            result.append(headerValues.stream().map(Object::toString).reduce("", (v1,v2) -> v1 + ", " + v2).substring(1));
            result.append("\n");
        });
        try {
            String responseBodyToString = IOUtils.toString(responseContext.getEntityStream(), StandardCharsets.UTF_8);
            responseContext.setEntityStream(new ByteArrayInputStream(responseBodyToString.getBytes(StandardCharsets.UTF_8.name())));
            result.append("BODY: ").append(responseBodyToString);
        } catch (IOException e) {

        }
        return result.toString();
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        LOG.log(INFO, responseToLogString(responseContext));
    }
}