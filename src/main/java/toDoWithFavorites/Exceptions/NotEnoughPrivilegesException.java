package toDoWithFavorites.Exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class NotEnoughPrivilegesException extends CustomException {
    @Override
    public ResponseEntity<Object> getResponseEntity(CustomException ex, WebRequest request) {
        return new ResponseEntity<Object>(new ExceptionOutput("Not enought privileges to create user"), new HttpHeaders(), HttpStatus.FORBIDDEN);

    }
}
