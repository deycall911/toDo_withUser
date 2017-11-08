package toDoWithFavorites;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import toDoWithFavorites.Exceptions.CustomException;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        return new ResponseEntity<Object>("{errorText: " + ex.getMessage() + "}", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<Object> handleAll(CustomException ex, WebRequest request) {
        return ex.getResponseEntity(ex, request);
    }

}
