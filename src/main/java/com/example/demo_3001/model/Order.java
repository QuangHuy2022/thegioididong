package com.example.demo_3001.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private double totalAmount;
    private double shippingFee;
    private int rewardPoints;
    private String status = "PENDING"; // PENDING, PAID, CANCELLED
    private String momoOrderId;
    private String momoRequestId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    public Order() {}

    public Order(Long id, String customerName, List<OrderDetail> orderDetails) {
        this.id = id;
        this.customerName = customerName;
        this.orderDetails = orderDetails;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMomoOrderId() { return momoOrderId; }
    public void setMomoOrderId(String momoOrderId) { this.momoOrderId = momoOrderId; }

    public String getMomoRequestId() { return momoRequestId; }
    public void setMomoRequestId(String momoRequestId) { this.momoRequestId = momoRequestId; }
}
