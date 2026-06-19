package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.LoginRequestDTO;
import com.library.dto.RegisterRequestDTO;
import com.library.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho AuthController: dang ky tai khoan moi va dang nhap.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @Test
    void register_KhiDuLieuHopLe_TraVe201VaToken() throws Exception {
        RegisterRequestDTO requestDTO = RegisterRequestDTO.builder()
                .username("nguyenvana")
                .password("matkhau123")
                .email("nguyenvana@example.com")
                .fullName("Nguyen Van A")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("nguyenvana"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void register_KhiUsernameDaTonTai_TraVe409Conflict() throws Exception {
        RegisterRequestDTO requestDTO = RegisterRequestDTO.builder()
                .username("tranthib")
                .password("matkhau123")
                .email("tranthib@example.com")
                .fullName("Tran Thi B")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        RegisterRequestDTO duplicateRequest = RegisterRequestDTO.builder()
                .username("tranthib")
                .password("matkhaukhac")
                .email("khac@example.com")
                .fullName("Nguoi Khac")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void login_KhiThongTinDungVaSai_TraVeKetQuaPhuHop() throws Exception {
        RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
                .username("levanc")
                .password("matkhaudung")
                .email("levanc@example.com")
                .fullName("Le Van C")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Dang nhap dung -> 200 OK kem token
        LoginRequestDTO correctLogin = LoginRequestDTO.builder()
                .username("levanc")
                .password("matkhaudung")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Dang nhap sai mat khau -> 401 Unauthorized
        LoginRequestDTO wrongLogin = LoginRequestDTO.builder()
                .username("levanc")
                .password("matkhausai")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
