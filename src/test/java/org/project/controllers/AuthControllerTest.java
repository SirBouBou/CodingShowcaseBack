/**package org.project.controllers;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.payload.request.LoginRequest;
import org.project.payload.request.SignupRequest;
import org.project.payload.response.MessageResponse;
import org.project.payload.response.UserInfoResponse;
import org.project.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;



    /*@Test
    public void createTestUser() {
        SignupRequest signupRequest = new SignupRequest("testName", "testEmail@test.com", null, "testPassword", 0);
        LoginRequest loginRequest = new LoginRequest("testName", "testPassword");
        ResponseEntity<MessageResponse> registerResponse =  authController.registerUser(signupRequest);
        assertTrue(registerResponse.getStatusCode().equals(200));
        ResponseEntity<UserInfoResponse> loginResponse = authController.authenticateUser(loginRequest);
        assertTrue(loginResponse.getStatusCode().equals(200));
        //ResponseEntity<MessageResponse> logoutResponse = authController.logoutUser(loginRequest);
        //assertTrue(logoutResponse.getStatusCode().equals(200));
    }/
}
*/