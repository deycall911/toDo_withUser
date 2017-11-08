package toDoWithFavorites.Exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class WrongUserException extends CustomException {
    private String reason;
    public WrongUserException(String reason) {
        this.reason = reason;
    }

    @Override
    public ResponseEntity<Object> getResponseEntity(CustomException ex, WebRequest request) {
        return new ResponseEntity<Object>(new ExceptionOutput(reason), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }
}
