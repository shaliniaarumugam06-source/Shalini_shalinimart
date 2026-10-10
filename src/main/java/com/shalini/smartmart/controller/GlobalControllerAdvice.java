package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.service.CartService;
import com.shalini.smartmart.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && "CUSTOMER".equalsIgnoreCase(user.getRole())) {
            try {
                return cartService.getCartForUser(user).getTotalItemCount();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    @ModelAttribute("allCategories")
    public List<Category> getAllCategories() {
        try {
            return productService.getAllCategories();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
