package com.shopflow.shopflow.controller;

import com.shopflow.shopflow.dto.request.OrderRequest;
import com.shopflow.shopflow.dto.response.OrderResponse;
import com.shopflow.shopflow.entity.Order;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.enums.Role;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.mapper.OrderMapper;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                orderMapper.toResponse(
                        orderService.placeOrder(user, request.getShippingAddress())
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(
                orderService.getAllOrders()
                        .stream()
                        .map(orderMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication auth) {
        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                orderService.getMyOrders(user)
                        .stream()
                        .map(orderMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Order order = orderService.getById(id);

        if (user.getRole() == Role.CUSTOMER && !order.getCustomer().getId().equals(user.getId())) {
            throw new BusinessException("Access denied: You can only view your own orders");
        }

        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                orderMapper.toResponse(orderService.cancelOrder(id, user))
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                orderMapper.toResponse(orderService.updateStatus(id, status))
        );
    }
}