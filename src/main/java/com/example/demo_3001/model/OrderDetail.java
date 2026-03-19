package com.example.demo_3001.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private double price;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderDetail() {}

    public OrderDetail(Long id, int quantity, Product product, Order order) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
        this.order = order;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
