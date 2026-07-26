package com.orderflow.payment.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionsHandler {

    private Map<String, Object> globalExceptionHeader(HttpStatus status, String error) {
        Map<String, Object> body = new LinkedHashMap<>();

        ZonedDateTime portugalTime = ZonedDateTime.now(ZoneId.of("Europe/Lisbon"));
        String formatted = portugalTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String timestamp = formatted + " PT(UTC" + portugalTime.getOffset().getId() + ")";

        body.put("timestamp", timestamp);
        body.put("status", status.value());
        body.put("error", error);
        return body;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, Object> body = globalExceptionHeader(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        body.put("message", "Something went wrong");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidStripeWebhookException.class)
    public ResponseEntity<Object> resourceNotFoundHandler(InvalidStripeWebhookException ex) {
        Map<String, Object> body = globalExceptionHeader(HttpStatus.BAD_REQUEST, "Bad Request");
        body.put("message", "Invalid Stripe webhook signature");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
