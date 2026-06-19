package com.library.controller;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import com.library.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho DashboardController: kiem tra quyen truy cap (chi ADMIN)
 * va so lieu thong ke duoc tinh dung tu Database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(3)
                .availableCopies(2)
                .category("Cong nghe thong tin")
                .build());
    }

    @Test
    @WithMockUser(username = "admin-test", roles = {"ADMIN"})
    void getStats_KhiLaAdmin_TraVe200VaSoLieuDung() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(1))
                .andExpect(jsonPath("$.totalCopies").value(3))
                .andExpect(jsonPath("$.totalAvailableCopies").value(2))
                .andExpect(jsonPath("$.totalBorrowedCopies").value(1));
    }

    @Test
    @WithMockUser(username = "member-test", roles = {"MEMBER"})
    void getStats_KhiLaMember_TraVe403Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getStats_KhiChuaXacThuc_TraVe401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
