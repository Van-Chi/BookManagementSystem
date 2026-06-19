package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookRequestDTO;
import com.library.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho BookController, khoi dong toan bo Spring Context
 * (su dung Database H2 in-memory duoc cau hinh tai src/test/resources/application.yml)
 * de kiem tra luong CRUD hoan chinh tu HTTP layer xuong toi Database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin-test", roles = {"ADMIN"})
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @Test
    void luongCrudHoanChinh_TaoDocCapNhatXoaSach_ThanhCong() throws Exception {
        BookRequestDTO createRequest = BookRequestDTO.builder()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("9780134685991")
                .totalCopies(3)
                .category("Cong nghe thong tin")
                .build();

        // 1. Tao moi sach -> 201 Created
        String responseJson = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.totalCopies").value(3))
                .andExpect(jsonPath("$.availableCopies").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(responseJson).get("id").asLong();

        // 2. Lay chi tiet sach vua tao -> 200 OK
        mockMvc.perform(get("/api/books/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780134685991"));

        // 3. Lay danh sach toan bo sach -> co chua sach vua tao (tra ve Page)
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));

        // 4. Cap nhat sach
        BookRequestDTO updateRequest = BookRequestDTO.builder()
                .title("Effective Java (3rd Edition)")
                .author("Joshua Bloch")
                .isbn("9780134685991")
                .totalCopies(5)
                .category("Cong nghe thong tin")
                .build();

        mockMvc.perform(put("/api/books/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java (3rd Edition)"))
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.availableCopies").value(5));

        // 5. Xoa sach -> 204 No Content
        mockMvc.perform(delete("/api/books/{id}", createdId))
                .andExpect(status().isNoContent());

        // 6. Lay lai sach da xoa -> 404 Not Found voi cau truc loi dong nhat
        mockMvc.perform(get("/api/books/{id}", createdId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/books/" + createdId));
    }

    @Test
    void createBook_KhiThieuDuLieuBatBuoc_TraVe400VaDanhSachLoi() throws Exception {
        BookRequestDTO invalidRequest = BookRequestDTO.builder()
                .title("")
                .author(null)
                .isbn("123")
                .totalCopies(-1)
                .category("")
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.author").exists())
                .andExpect(jsonPath("$.errors.isbn").exists())
                .andExpect(jsonPath("$.errors.totalCopies").exists());
    }

    @Test
    void createBook_KhiIsbnDaTonTai_TraVe409Conflict() throws Exception {
        BookRequestDTO requestDTO = BookRequestDTO.builder()
                .title("Domain-Driven Design")
                .author("Eric Evans")
                .isbn("9780321125217")
                .totalCopies(2)
                .category("Cong nghe thong tin")
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        // Tao lan thu hai voi cung ISBN -> phai bi tu choi
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void searchBooks_VoiKeyword_ChiTraVeSachKhop() throws Exception {
        taoSach("Effective Java", "Joshua Bloch", "9780134685991", 3, "Cong nghe thong tin");
        taoSach("Design Patterns", "Gang of Four", "9780201633610", 2, "Kien truc phan mem");

        mockMvc.perform(get("/api/books").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Effective Java")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void searchBooks_VoiCategory_ChiTraVeSachCungTheLoai() throws Exception {
        taoSach("Effective Java", "Joshua Bloch", "9780134685991", 3, "Cong nghe thong tin");
        taoSach("Design Patterns", "Gang of Four", "9780201633610", 2, "Kien truc phan mem");

        mockMvc.perform(get("/api/books").param("category", "Kien truc phan mem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Design Patterns")));
    }

    @Test
    void searchBooks_VoiAvailableTrue_ChiTraVeSachConKhaDung() throws Exception {
        taoSach("Effective Java", "Joshua Bloch", "9780134685991", 3, "CNTT");
        taoSach("Sach Het", "Tac Gia A", "9780000000001", 0, "CNTT");

        mockMvc.perform(get("/api/books").param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Effective Java")));
    }

    @Test
    void searchBooks_VoiKeywordVaCategory_LocKetHopChinhXac() throws Exception {
        taoSach("Effective Java", "Joshua Bloch", "9780134685991", 3, "Cong nghe thong tin");
        taoSach("Java Performance", "Scott Oaks", "9781449358457", 2, "Cong nghe thong tin");
        taoSach("Design Patterns", "Gang of Four", "9780201633610", 2, "Kien truc phan mem");

        mockMvc.perform(get("/api/books")
                        .param("keyword", "java")
                        .param("category", "Cong nghe thong tin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void searchBooks_VoiPhanTrang_TraVeMetadataDung() throws Exception {
        taoSach("Book A", "Author 1", "9780000000011", 1, "CNTT");
        taoSach("Book B", "Author 2", "9780000000012", 1, "CNTT");
        taoSach("Book C", "Author 3", "9780000000013", 1, "CNTT");

        mockMvc.perform(get("/api/books").param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.size", is(2)));
    }

    @Test
    void searchBooks_KhongCoFilter_TraVeToanBoSach() throws Exception {
        taoSach("Effective Java", "Joshua Bloch", "9780134685991", 3, "CNTT");
        taoSach("Design Patterns", "Gang of Four", "9780201633610", 2, "CNTT");

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    private void taoSach(String title, String author, String isbn, int totalCopies, String category) throws Exception {
        BookRequestDTO req = BookRequestDTO.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .totalCopies(totalCopies)
                .category(category)
                .build();
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }
}
