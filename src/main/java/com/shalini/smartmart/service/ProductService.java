package com.shalini.smartmart.service;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.repository.CategoryRepository;
import com.shalini.smartmart.repository.OrderItemRepository;
import com.shalini.smartmart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) throws Exception {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new Exception("Product name cannot be empty.");
        }
        if (product.getPrice() == null || product.getPrice() <= 0.0) {
            throw new Exception("Product price must be greater than zero.");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new Exception("Stock quantity cannot be negative.");
        }
        if (product.getDiscount() == null || product.getDiscount() < 0.0 || product.getDiscount() > 100.0) {
            throw new Exception("Discount percentage must be between 0% and 100%.");
        }
        if (product.getCategory() == null) {
            throw new Exception("Product must belong to a valid category.");
        }

        return productRepository.save(product);
    }

    public void updateStock(Long productId, Integer newStock) throws Exception {
        if (newStock == null || newStock < 0) {
            throw new Exception("Stock quantity cannot be negative.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found."));
        product.setStock(newStock);
        productRepository.save(product);
    }

    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Product not found."));

        if (orderItemRepository.existsByProductId(id)) {
            throw new Exception("Cannot delete product '" + product.getName() + "' because it is recorded in historical customer orders. You can set its stock to 0 to mark it out of stock instead.");
        }

        productRepository.delete(product);
    }

    public List<Product> searchProducts(String keyword, Long categoryId) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = categoryId != null && categoryId > 0;

        if (hasKeyword && hasCategory) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryId(keyword.trim(), categoryId);
        } else if (hasKeyword) {
            return productRepository.findByNameContainingIgnoreCase(keyword.trim());
        } else if (hasCategory) {
            return productRepository.findByCategoryId(categoryId);
        } else {
            return productRepository.findAll();
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) throws Exception {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new Exception("Category name cannot be empty.");
        }
        category.setName(category.getName().trim());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Category not found."));

        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new Exception("Cannot delete category '" + category.getName() + "' because " + productCount + " products are currently assigned to it. Please reassign or delete these products first.");
        }

        categoryRepository.delete(category);
    }

    public long countProducts() {
        return productRepository.count();
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockLessThanEqual(threshold);
    }
}
