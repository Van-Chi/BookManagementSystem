package com.library.controller;

import com.library.dto.DashboardStatsDTO;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cung cap API thong ke tong quan cho man hinh Dashboard.
 * Chi danh cho ADMIN, duoc gioi han quyen truy cap trong SecurityConfig.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Lay so lieu thong ke tong quan: tong so sach, so ban sach con/da muon,
     * so nguoi dung, so phieu muon dang hoat dong/qua han/da tra, top sach muon nhieu nhat.
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
