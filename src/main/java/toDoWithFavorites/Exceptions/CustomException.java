package toDoWithFavorites.Exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class CustomException extends Exception {
    public ResponseEntity<Object> getResponseEntity(CustomException ex, WebRequest request) {
        return new ResponseEntity<Object>(new ExceptionOutput(ex.getMessage()), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    };

    public class ExceptionOutput {
        ExceptionOutput(String errorText) {
            this.errorText = errorText;
            this.errorOnThread = Thread.currentThread().getId();
        }
        public String errorText;
        public long errorOnThread;
    }
}
