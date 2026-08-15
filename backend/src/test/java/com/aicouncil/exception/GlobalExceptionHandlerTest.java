package com.aicouncil.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    @DisplayName("Should handle DebateNotFoundException with 404 response")
    void testHandleDebateNotFoundException() {
        DebateNotFoundException ex = new DebateNotFoundException("Debate 123 missing");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDebateNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("NOT_FOUND", body.get("error"));
        assertEquals("Debate 123 missing", body.get("message"));
        assertEquals("/api/test", body.get("path"));
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 response")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid provider ID");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("INVALID_ARGUMENT", body.get("error"));
    }

    @Test
    @DisplayName("Should handle Generic Exception with 500 response and zero sensitive stack traces")
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Database error or secret string");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("INTERNAL_SERVER_ERROR", body.get("error"));
        assertEquals("An unexpected error occurred. Please contact system support.", body.get("message"));
    }
}
