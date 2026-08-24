package com.nexcart.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExists(
        CategoryAlreadyExistsException ex,
        HttpServletRequest request){

            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
            );

            return ResponseEntity
                   .status(HttpStatus.CONFLICT)
                    .body(error);
        }
    
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
        CategoryNotFoundException ex,
        HttpServletRequest request){

            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
            );

            return ResponseEntity
                   .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request){

            String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI()
            );

            return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }


    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
        ProductNotFoundException ex,
        HttpServletRequest request){

            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
            );

            return ResponseEntity
                   .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUse(
        CategoryInUseException ex,
        HttpServletRequest request){

            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
            );

            return ResponseEntity
                   .status(HttpStatus.CONFLICT)
                    .body(error);
        }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
        UserAlreadyExistsException ex,
        HttpServletRequest request){

            ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
            );

            return ResponseEntity
                   .status(HttpStatus.CONFLICT)
                    .body(error);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                BadCredentialsException ex,
                HttpServletRequest request){

                    ErrorResponse error = new ErrorResponse(
                            LocalDateTime.now(),
                            HttpStatus.UNAUTHORIZED.value(),
                            "Invalid email or password",
                            request.getRequestURI()
                    );

                    return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(error);
                }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMessageNotReadable(
                HttpMessageNotReadableException ex,
                HttpServletRequest request){

                    ErrorResponse error = new ErrorResponse(
                            LocalDateTime.now(),
                            HttpStatus.BAD_REQUEST.value(),
                            "Malformed request body.",
                            request.getRequestURI()
                    );

                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                        .body(error);
                }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                MethodArgumentTypeMismatchException ex,
                HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                "Invalid parameter type in request URL.",
                        request.getRequestURI()
                    );

                    return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
                }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request){

                ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage(),
                    request.getRequestURI()
                );

                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
            }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage(),
                    request.getRequestURI()
                );

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(error);
            }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request){

                ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected server error occurred.",
                    request.getRequestURI()
                );

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(error);
            } 
}
