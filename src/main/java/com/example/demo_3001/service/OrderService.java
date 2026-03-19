package com.example.demo_3001.service;

import com.example.demo_3001.model.*;
import com.example.demo_3001.repository.OrderRepository;
import com.example.demo_3001.repository.OrderDetailRepository;
import com.example.demo_3001.repository.ProductRepository;
import com.example.demo_3001.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order createOrder(User user, List<CartItem> cartItems, String customerName, Voucher voucher) {
        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(customerName);
        
        double subtotal = 0;
        int totalQuantity = 0;
        List<OrderDetail> orderDetails = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            
            double itemTotal;
            if (product.isPromotion()) {
                int promoQty = product.getPromotionQuantity();
                if (promoQty > 0) {
                    int availablePromo = Math.min(quantity, promoQty);
                    int overQty = Math.max(0, quantity - promoQty);
                    
                    itemTotal = (product.getPrice() * availablePromo) + (product.getOriginalPrice() * overQty);
                    
                    // Trừ số lượng khuyến mãi
                    product.setPromotionQuantity(promoQty - availablePromo);
                    productRepository.save(product);
                } else {
                    itemTotal = product.getOriginalPrice() * quantity;
                }
            } else {
                itemTotal = product.getPrice() * quantity;
            }

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setPrice(itemTotal / quantity); // Giá trung bình cho mỗi item
            detail.setOrder(order);
            orderDetails.add(detail);

            subtotal += itemTotal;
            totalQuantity += quantity;
        }

        double shippingFee = (subtotal >= 1000000 && totalQuantity >= 2) ? 0 : 30000;
        double totalAmount = subtotal + shippingFee;
        
        if (voucher != null && subtotal >= voucher.getMinOrderAmount()) {
            totalAmount -= voucher.getDiscountAmount();
            // Remove voucher from user
            if (user != null) {
                user.getVouchers().remove(voucher);
            }
        }

        int rewardPoints = (int) (subtotal / 15000) * 2;

        order.setTotalAmount(Math.max(0, totalAmount));
        order.setShippingFee(shippingFee);
        order.setRewardPoints(rewardPoints);
        order.setOrderDetails(orderDetails);

        if (user != null) {
            user.setRewardPoints(user.getRewardPoints() + rewardPoints);
            userRepository.save(user);
        }

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public java.util.Optional<Order> getOrderByMomoOrderId(String momoOrderId) {
        return orderRepository.findByMomoOrderId(momoOrderId);
    }

    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if ("PENDING".equals(order.getStatus())) {
                order.setStatus("CANCELLED");
                orderRepository.save(order);
            }
        });
    }
}
