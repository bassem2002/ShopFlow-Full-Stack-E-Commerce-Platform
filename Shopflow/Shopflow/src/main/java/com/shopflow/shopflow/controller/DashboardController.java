package com.shopflow.shopflow.controller;

import com.shopflow.shopflow.dto.response.DashboardDto;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDto.AdminDashboard> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<DashboardDto.SellerDashboard> getSellerDashboard(
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(dashboardService.getSellerDashboard(seller.getId()));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<DashboardDto.CustomerDashboard> getCustomerDashboard(
            @AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(dashboardService.getCustomerDashboard(customer.getId()));
    }
}
