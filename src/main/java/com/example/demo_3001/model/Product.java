package com.example.demo_3001.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private String description;
    private String image;
    private int discount;

    private String productType = "NONE";

    private boolean isPromotion = false;
    private int promotionQuantity = 0;
    private double originalPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Product() {}

    public Product(Long id, String name, double price, String description, String image, int discount, String productType, Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
        this.discount = discount;
        this.productType = productType;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public boolean isPromotion() { return isPromotion; }
    public void setPromotion(boolean promotion) { isPromotion = promotion; }

    public int getPromotionQuantity() { return promotionQuantity; }
    public void setPromotionQuantity(int promotionQuantity) { this.promotionQuantity = promotionQuantity; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
}
