package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.RateLimitingFilter;
import com.library.dto.LoginRequestDTO;
import com.library.dto.RegisterRequestDTO;
import com.library.entity.Book;
import com.library.repository.BookRepository;
import com.library.service.EmailService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho RateLimitingFilter.
 * Dùng @TestPropertySource để tạo context riêng với giới hạn thấp, tránh ảnh hưởng test cũ.
 * Xoá bucket store trước mỗi test để đảm bảo trạng thái sạch.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "rate-limit.auth-requests-per-minute=2",
        "rate-limit.anonymous-requests-per-minute=2",
        "rate-limit.authenticated-requests-per-minute=3"
})
class RateLimitingFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private ConcurrentHashMap<String, Bucket> rateLimitBucketStore;

    private Long seededBookId;

    @BeforeEach
    void setUp() {
        // Xoá toàn bộ bucket để mỗi test bắt đầu với trạng thái sạch
        rateLimitBucketStore.clear();

        Book book = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .availableCopies(5)
                .category("Cong nghe")
                .build();
        seededBookId = bookRepository.save(book).getId();
    }

    // =========================================================
    // Test 1: Request trong giới hạn — phải pass
    // =========================================================

    @Test
    @WithMockUser(username = "normal-user", roles = {"MEMBER"})
    void authenticatedUser_TrongGioiHan_TraVe200() throws Exception {
        // authenticated-requests-per-minute=3, gửi 3 request → tất cả phải pass
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/books/" + seededBookId))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================
    // Test 2: Request thứ N+1 — phải trả về 429
    // =========================================================

    @Test
    @WithMockUser(username = "rate-limited-user", roles = {"MEMBER"})
    void authenticatedUser_VuotGioiHan_TraVe429() throws Exception {
        // authenticated-requests-per-minute=3, gửi 3 request hợp lệ
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/books/" + seededBookId))
                    .andExpect(status().isOk());
        }

        // Request thứ 4 phải bị chặn
        mockMvc.perform(get("/api/books/" + seededBookId))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.retryAfter").value(60))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("Retry-After", "60"));
    }

    // =========================================================
    // Test 3: ADMIN không bị chặn dù vượt giới hạn
    // =========================================================

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void adminUser_VuotGioiHan_KhongBiChan() throws Exception {
        // Gửi 10 request (nhiều hơn mọi giới hạn cấu hình) — ADMIN không bị chặn
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/books/" + seededBookId))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================
    // Test 4: Auth endpoint bị giới hạn theo IP (chống brute force)
    // =========================================================

    @Test
    void authEndpoint_VuotGioiHan_TraVe429() throws Exception {
        // auth-requests-per-minute=2, IP duy nhất cho test này
        String uniqueIp = "10.0.1.1";
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .username("khong-ton-tai")
                .password("sai-mat-khau")
                .build();

        // 2 request đầu — được phép (dù login thất bại, rate limit vẫn tính)
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", uniqueIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized()); // 401, chưa bị rate limit
        }

        // Request thứ 3 — bị chặn bởi rate limit
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", uniqueIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    // =========================================================
    // Test 5: Anonymous user bị giới hạn theo IP
    // =========================================================

    @Test
    void anonymousUser_VuotGioiHan_TraVe429() throws Exception {
        // anonymous-requests-per-minute=2, dùng IP khác để tránh ảnh hưởng test khác
        String uniqueIp = "10.0.2.1";

        // /api/auth/register không phải auth endpoint duy nhất, nhưng ta dùng endpoint khác
        // Dùng /api/books (anonymous) để test
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/api/books/" + seededBookId)
                            .header("X-Forwarded-For", uniqueIp))
                    .andExpect(status().isUnauthorized()); // 401 vì chưa đăng nhập, nhưng rate limit chưa kích hoạt
        }

        // Request thứ 3 — bị rate limit trước khi kiểm tra auth
        mockMvc.perform(get("/api/books/" + seededBookId)
                        .header("X-Forwarded-For", uniqueIp))
                .andExpect(status().isTooManyRequests());
    }

    // =========================================================
    // Test 6: Authenticated user với JWT thực — gửi đủ request rồi bị chặn
    // =========================================================

    @Test
    void authenticatedUserVoiJWT_VuotGioiHan_TraVe429() throws Exception {
        String token = registerAndGetToken("ratelimit-jwt-user");

        // authenticated-requests-per-minute=3, gửi 3 request
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/books/" + seededBookId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk());
        }

        // Request thứ 4 bị chặn
        mockMvc.perform(get("/api/books/" + seededBookId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.retryAfter").value(60));
    }

    // =========================================================
    // Test 7: Header X-RateLimit-Remaining giảm dần đúng
    // =========================================================

    @Test
    @WithMockUser(username = "header-check-user", roles = {"MEMBER"})
    void xRateLimitRemainingHeader_GiamDanDung() throws Exception {
        // authenticated-requests-per-minute=3
        mockMvc.perform(get("/api/books/" + seededBookId))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Remaining"));

        // Sau request thứ 2, header remaining phải là 1
        mockMvc.perform(get("/api/books/" + seededBookId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "1"));

        // Sau request thứ 3, remaining = 0
        mockMvc.perform(get("/api/books/" + seededBookId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }

    private String registerAndGetToken(String username) throws Exception {
        RegisterRequestDTO requestDTO = RegisterRequestDTO.builder()
                .username(username)
                .password("matkhau123")
                .email(username + "@example.com")
                .fullName("Rate Limit Test User")
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
}
