package org.project.security.jwt;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationException authException;

    @Test
    void commence_shouldReturnUnauthorizedResponse() throws Exception {
        // Arrange
        AuthEntryPointJwt entryPoint = new AuthEntryPointJwt(objectMapper);

        String message = "Invalid JWT token";
        String path = "/api/users";

        when(authException.getMessage()).thenReturn(message);
        when(request.getServletPath()).thenReturn(path);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) {
                output.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(
                    jakarta.servlet.WriteListener writeListener) {
            }
        };

        when(response.getOutputStream()).thenReturn(servletOutputStream);

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JsonNode json = new ObjectMapper()
                .readTree(output.toByteArray());

        assertEquals(401, json.get("status").asInt());
        assertEquals("Unauthorized", json.get("error").asText());
        assertEquals(message, json.get("message").asText());
        assertEquals(path, json.get("path").asText());
    }
}