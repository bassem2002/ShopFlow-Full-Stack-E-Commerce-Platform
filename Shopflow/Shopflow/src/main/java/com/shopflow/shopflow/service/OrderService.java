package com.shopflow.shopflow.service;

import com.shopflow.shopflow.entity.Order;
import com.shopflow.shopflow.entity.User;

import java.util.List;

public interface OrderService {

    Order placeOrder(User user, String address);

    List<Order> getMyOrders(User user);
    
    List<Order> getAllOrders();

    Order getById(Long id);

    Order cancelOrder(Long id, User user);

    Order updateStatus(Long id, String status);
}