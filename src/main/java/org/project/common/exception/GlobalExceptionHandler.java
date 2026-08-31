package org.project.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.project.auth.exception.InvalidPlayerNameException;
import org.project.common.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPlayerNameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPlayerName(
            InvalidPlayerNameException exception, HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Bad request",
                        exception.getMessage(),
                        request.getServletPath()
                ));
    }
}
