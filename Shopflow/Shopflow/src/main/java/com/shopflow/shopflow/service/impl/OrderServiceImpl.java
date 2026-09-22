package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.entity.*;
import com.shopflow.shopflow.enums.OrderStatus;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.*;
import com.shopflow.shopflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Override
    public Order placeOrder(User customer, String address) {

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new BusinessException("Panier introuvable"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Panier vide");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(address);
        order.setCreatedAt(LocalDateTime.now());

        double subTotal = 0;
        List<OrderItem> items = new ArrayList<>();

        for (CartItem ci : cart.getItems()) {

            Product p = ci.getProduct();
            ProductVariant v = ci.getVariant();

            int q = ci.getQuantity();

            double price = (p.getPromoPrice() != null)
                    ? p.getPromoPrice()
                    : p.getPrice();

            if (v != null) {
                price += v.getPriceDelta();

                if (v.getStockAdditional() < q)
                    throw new BusinessException("Stock insuffisant variante");

                v.setStockAdditional(v.getStockAdditional() - q);
            } else {
                if (p.getStock() < q)
                    throw new BusinessException("Stock insuffisant produit");

                p.setStock(p.getStock() - q);
            }

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .variant(v)
                    .quantity(q)
                    .unitPrice(price)
                    .build();

            subTotal += price * q;
            items.add(oi);
        }

        double total = subTotal;

        if (cart.getCoupon() != null) {
            if (cart.getCoupon().getType().name().equals("PERCENT")) {
                total -= subTotal * cart.getCoupon().getValue() / 100;
            } else {
                total -= cart.getCoupon().getValue();
            }
        }

        double shipping = 10.0;

        order.setSubTotal(subTotal);
        order.setShippingCost(shipping);
        order.setTotal(total + shipping);
        order.setItems(items);

        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cart.setCoupon(null);

        return saved;
    }

    @Override
    public List<Order> getMyOrders(User user) {
        if (user.getRole().name().equals("SELLER")) {
            return orderRepository.findOrdersBySellerId(user.getId());
        } else if (user.getRole().name().equals("ADMIN")) {
            return orderRepository.findAll();
        }
        return orderRepository.findByCustomer(user);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Commande non trouvée"));
    }

    @Override
    public Order cancelOrder(Long id, User user) {

        Order order = getById(id);

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé");
        }

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Annulation impossible");
        }

        // Restauration du stock avant l'annulation effective
        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }

    @Override
    public Order updateStatus(Long id, String status) {

        Order order = getById(id);
        OrderStatus newStatus = OrderStatus.valueOf(status);

        // Si on passe à CANCELLED et que ce n'était pas déjà le cas
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);

        return order;
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.setStockAdditional(variant.getStockAdditional() + item.getQuantity());
            } else {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
            }
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().getYear() + "-" + System.currentTimeMillis();
    }
}