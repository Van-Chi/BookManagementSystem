package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BorrowRequestDTO;
import com.library.dto.RegisterRequestDTO;
import com.library.entity.Book;
import com.library.repository.BookRepository;
import com.library.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho BorrowController: kiem tra luong muon/tra sach
 * hoan chinh, bao gom xac thuc bang JWT thuc su (lay tu API dang ky).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BorrowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @MockBean
    private EmailService emailService;

    private Long seededBookId;

    @BeforeEach
    void setUp() {
        Book book = Book.builder()
                .title("The Pragmatic Programmer")
                .author("David Thomas")
                .isbn("9780135957059")
                .totalCopies(1)
                .availableCopies(1)
                .category("Cong nghe thong tin")
                .build();
        seededBookId = bookRepository.save(book).getId();
    }

    private String registerAndGetToken(String username) throws Exception {
        RegisterRequestDTO requestDTO = RegisterRequestDTO.builder()
                .username(username)
                .password("matkhau123")
                .email(username + "@example.com")
                .fullName("Nguoi dung " + username)
                .build();

        String responseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson).get("token").asText();
    }

    @Test
    void luongMuonTraSach_HoanChinh_ThanhCong() throws Exception {
        String token = registerAndGetToken("borroweruser");

        BorrowRequestDTO borrowRequest = BorrowRequestDTO.builder().bookId(seededBookId).build();

        // 1. Muon sach -> 201 Created, availableCopies giam tu 1 xuong 0
        String borrowResponseJson = mockMvc.perform(post("/api/borrows")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(seededBookId))
                .andExpect(jsonPath("$.status").value("BORROWED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long borrowRecordId = objectMapper.readTree(borrowResponseJson).get("id").asLong();

        Book bookAfterBorrow = bookRepository.findById(seededBookId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(bookAfterBorrow.getAvailableCopies()).isEqualTo(0);

        // 2. Muon tiep sach da het ban -> 409 Conflict
        mockMvc.perform(post("/api/borrows")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isConflict());

        // 3. Xem lich su muon sach cua minh
        mockMvc.perform(get("/api/borrows/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(seededBookId));

        // 4. Tra sach -> 200 OK, availableCopies tang tro lai 1
        mockMvc.perform(put("/api/borrows/{id}/return", borrowRecordId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        Book bookAfterReturn = bookRepository.findById(seededBookId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(bookAfterReturn.getAvailableCopies()).isEqualTo(1);

        // 5. Tra lai lan nua -> 400 Bad Request (da tra truoc do)
        mockMvc.perform(put("/api/borrows/{id}/return", borrowRecordId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void borrowBook_KhiKhongGuiToken_TraVe401Unauthorized() throws Exception {
        BorrowRequestDTO borrowRequest = BorrowRequestDTO.builder().bookId(seededBookId).build();

        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void borrowBook_KhiSachKhongTonTai_TraVe404NotFound() throws Exception {
        String token = registerAndGetToken("borroweruser2");

        BorrowRequestDTO borrowRequest = BorrowRequestDTO.builder().bookId(999999L).build();

        mockMvc.perform(post("/api/borrows")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isNotFound());
    }
}
