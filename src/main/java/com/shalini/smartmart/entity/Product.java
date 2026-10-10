package com.shalini.smartmart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    private Double discount = 0.0; // percentage e.g. 10.0 for 10%

    @Column(nullable = false)
    private Integer stock;

    private String image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    public Product() {}

    public Product(String name, String description, Double price, Double discount, Integer stock, String image, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount != null ? discount : 0.0;
        this.stock = stock;
        this.image = image;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // Helper method to compute final price after discount
    public Double getFinalPrice() {
        if (discount == null || discount <= 0) {
            return price;
        }
        double finalP = price - (price * discount / 100.0);
        return Math.round(finalP * 100.0) / 100.0;
    }
}
