package com.shalini.smartmart.config;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.repository.CategoryRepository;
import com.shalini.smartmart.repository.ProductRepository;
import com.shalini.smartmart.repository.UserRepository;
import com.shalini.smartmart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize Primary Admin account: admin@smartmart.com / admin123
        userService.createAdminIfNotExists(
                "Shalini Admin",
                "admin@smartmart.com",
                "admin123",
                "9876543210",
                "Shalini SmartMart Corporate Office, MG Road"
        );

        // Also alias admin@shmalmart.com for backwards compatibility with any template typos
        userService.createAdminIfNotExists(
                "Shalini Admin",
                "admin@shmalmart.com",
                "admin123",
                "9876543210",
                "Shalini SmartMart Corporate Office, MG Road"
        );

        // Initialize Sample Customer: customer@gmail.com / customer123
        if (!userRepository.existsByEmailIgnoreCase("customer@gmail.com")) {
            User customer = new User(
                    "Priya Sharma",
                    "customer@gmail.com",
                    "customer123",
                    "9123456789",
                    "Flat 302, Sunrise Apartments, College Road",
                    "CUSTOMER"
            );
            userService.registerCustomer(customer);
        }

        // Seed Default Categories if none exist
        if (categoryRepository.count() == 0) {
            String[] categoryNames = {
                    "Groceries",
                    "Fruits & Vegetables",
                    "Dairy",
                    "Snacks",
                    "Beverages",
                    "Personal Care",
                    "Home Care"
            };

            Map<String, Category> categoryMap = new HashMap<>();
            for (String catName : categoryNames) {
                Category cat = categoryRepository.save(new Category(catName));
                categoryMap.put(catName, cat);
            }

            // Seed 15 Sample Products if none exist
            if (productRepository.count() == 0) {
                createProduct("Royal Basmati Rice (5kg)", "Premium long-grain aged basmati rice for delicious biryanis and everyday meals.", 450.0, 10.0, 50, "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&auto=format&fit=crop", categoryMap.get("Groceries"));
                createProduct("Whole Wheat Atta (10kg)", "100% pure whole wheat flour processed from high-grade golden grains.", 380.0, 5.0, 40, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop", categoryMap.get("Groceries"));
                createProduct("Refined Sugar (1kg)", "Clean, ultra-pure crystal white sugar for daily hot beverages and baking.", 50.0, 0.0, 100, "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=500&auto=format&fit=crop", categoryMap.get("Groceries"));
                createProduct("Fresh Farm Milk (1L)", "Pure, pasteurized farm fresh cow milk packed with calcium and nutrients.", 65.0, 0.0, 30, "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500&auto=format&fit=crop", categoryMap.get("Dairy"));
                createProduct("Whole Wheat Bread (400g)", "Soft, freshly baked whole grain brown bread rich in natural dietary fiber.", 40.0, 5.0, 25, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop", categoryMap.get("Dairy"));
                createProduct("Premium Butter Biscuits", "Crispy butter cookies baked to golden perfection. Ideal teatime snack.", 30.0, 10.0, 60, "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&auto=format&fit=crop", categoryMap.get("Snacks"));
                createProduct("Fresh Red Apples (1kg)", "Sweet, juicy, handpicked Shimla red apples full of freshness and fiber.", 180.0, 15.0, 35, "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&auto=format&fit=crop", categoryMap.get("Fruits & Vegetables"));
                createProduct("Organic Bananas (1 Dozen)", "Naturally ripened, nutrient-dense fresh farm bananas.", 60.0, 0.0, 50, "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&auto=format&fit=crop", categoryMap.get("Fruits & Vegetables"));
                createProduct("Fresh Hybrid Tomatoes (1kg)", "Firm, red tomatoes grown organically, perfect for salads and gravies.", 40.0, 10.0, 45, "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&auto=format&fit=crop", categoryMap.get("Fruits & Vegetables"));
                createProduct("Herbal Nourishing Shampoo (340ml)", "Enriched with aloe vera and amla for smooth, shiny, and strong hair.", 290.0, 20.0, 20, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=500&auto=format&fit=crop", categoryMap.get("Personal Care"));
                createProduct("Moisturizing Beauty Soap (Pack of 4)", "Gentle cleansing soap with natural almond oil and glycerine.", 140.0, 10.0, 40, "https://images.unsplash.com/photo-1607006482172-386053334e40?w=500&auto=format&fit=crop", categoryMap.get("Personal Care"));
                createProduct("Fresh Mint Toothpaste (150g)", "Complete oral defense toothpaste providing 12-hour fresh breath and strong teeth.", 95.0, 15.0, 50, "https://images.unsplash.com/photo-1559598467-f8b76c8155d0?w=500&auto=format&fit=crop", categoryMap.get("Personal Care"));
                createProduct("Washing Powder Detergent (2kg)", "Advanced stain removal powder with fresh citrus scent for brilliant whites.", 320.0, 12.0, 30, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop", categoryMap.get("Home Care"));
                createProduct("Premium Instant Coffee (100g)", "Rich, aromatic 100% pure Arabica coffee granules for an instant boost.", 275.0, 10.0, 25, "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500&auto=format&fit=crop", categoryMap.get("Beverages"));
                createProduct("Real Orange Juice (1L)", "Refreshingly sweet 100% natural orange fruit juice with zero added preservatives.", 110.0, 8.0, 35, "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=500&auto=format&fit=crop", categoryMap.get("Beverages"));
            }
        }
    }

    private void createProduct(String name, String desc, Double price, Double discount, Integer stock, String img, Category category) {
        Product p = new Product(name, desc, price, discount, stock, img, category);
        productRepository.save(p);
    }
}
