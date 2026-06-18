package com.library.service;

import com.library.dto.DashboardStatsDTO;

/**
 * Service cung cap so lieu thong ke tong quan cho man hinh Dashboard cua ADMIN.
 */
public interface DashboardService {

    DashboardStatsDTO getStats();
}
