package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        List<Category> categories = productService.getAllCategories();
        List<Product> products = productService.getAllProducts();

        model.addAttribute("categories", categories);
        // Featured products (show up to 8 products on home page)
        model.addAttribute("featuredProducts", products.size() > 8 ? products.subList(0, 8) : products);
        return "index";
    }
}
