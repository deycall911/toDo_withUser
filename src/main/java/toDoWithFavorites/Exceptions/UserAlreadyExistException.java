package toDoWithFavorites.Exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class UserAlreadyExistException extends CustomException {
    @Override
    public ResponseEntity<Object> getResponseEntity(CustomException ex, WebRequest request) {
        return new ResponseEntity<Object>(new ExceptionOutput("User with that username already exist"), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
