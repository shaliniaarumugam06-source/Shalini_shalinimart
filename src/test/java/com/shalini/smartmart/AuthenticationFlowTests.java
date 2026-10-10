package com.shalini.smartmart;

import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.repository.UserRepository;
import com.shalini.smartmart.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void testCustomerRegistrationAndLoginFlow() throws Exception {
        String testEmail = "testuser" + System.currentTimeMillis() + "@gmail.com";

        // 1. Register new customer
        mockMvc.perform(post("/register")
                .param("name", "Test Customer")
                .param("email", testEmail)
                .param("phone", "9876543210")
                .param("password", "secret123")
                .param("confirmPassword", "secret123")
                .param("address", "123 Test Street, City"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("success"));

        // Verify saved in DB
        assertTrue(userRepository.findByEmail(testEmail).isPresent());

        // 2. Duplicate registration should fail
        mockMvc.perform(post("/register")
                .param("name", "Test Customer")
                .param("email", testEmail)
                .param("phone", "9876543210")
                .param("password", "secret123")
                .param("confirmPassword", "secret123")
                .param("address", "123 Test Street, City"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("error"));

        // 3. Customer Login with correct credentials
        mockMvc.perform(post("/login")
                .param("email", testEmail)
                .param("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("success"));

        // 4. Customer Login with incorrect password
        mockMvc.perform(post("/login")
                .param("email", testEmail)
                .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testAdminLogin() throws Exception {
        // Admin login with configured admin
        mockMvc.perform(post("/admin/login")
                .param("email", "admin@smartmart.com")
                .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        // Admin login with wrong credentials
        mockMvc.perform(post("/admin/login")
                .param("email", "admin@smartmart.com")
                .param("password", "wrongadminpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"))
                .andExpect(flash().attributeExists("error"));
    }
}
