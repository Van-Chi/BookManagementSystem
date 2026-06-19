package com.library.controller;

import com.library.dto.DashboardStatsDTO;
import com.library.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "Thống kê tổng quan cho ADMIN")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Lấy số liệu thống kê tổng quan (ADMIN)", description = "Trả về tổng số sách, bản sách còn/đã mượn, số người dùng, phiếu mượn theo trạng thái và top sách được mượn nhiều nhất")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về dashboard stats"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN")
    })
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
