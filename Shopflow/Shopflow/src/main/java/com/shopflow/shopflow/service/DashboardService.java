package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.response.DashboardDto;

public interface DashboardService {
    DashboardDto.AdminDashboard getAdminDashboard();
    DashboardDto.SellerDashboard getSellerDashboard(Long sellerId);
    DashboardDto.CustomerDashboard getCustomerDashboard(Long customerId);
}
