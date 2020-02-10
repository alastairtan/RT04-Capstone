package capstone.rt04.retailbackend.controllerExceptionHandlers;

import capstone.rt04.retailbackend.response.GenericErrorResponse;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.customer.*;
import capstone.rt04.retailbackend.util.exceptions.product.ProductVariantNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice //global exception handler for controllers (methods with a @RequestMapping etc annotation)
@RestController
public class CustomerExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    public final ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errorMsg = new HashMap<>();
        errorMsg.put("message", "Your input is invalid!");
        return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleInputDataValidationException(InputDataValidationException ex, WebRequest req) {
        return new ResponseEntity<>(ex.getErrorMap(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({CreateNewCustomerException.class, CustomerCannotDeleteException.class,})
    public final ResponseEntity<Object> handlePersistenceExceptions(Exception ex, WebRequest req) {
        return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({CustomerNotFoundException.class, CreditCardNotFoundException.class,
            AddressNotFoundException.class, ProductVariantNotFoundException.class})
    public final ResponseEntity<Object> handleNotFoundExceptions(Exception ex, WebRequest req) {
        return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InvalidLoginCredentialsException.class, CustomerNotVerifiedException.class})
    public final ResponseEntity<Object> handleUnauthorizedExceptions(Exception ex, WebRequest req) {
        return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({VerificationCodeInvalidException.class})
    public final ResponseEntity<Object> handleBadRequestExceptions(Exception ex, WebRequest req) {
        return new ResponseEntity<>(new GenericErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


}
