package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String search,
                               @RequestParam(required = false) Long category,
                               Model model) {
        List<Product> products = productService.searchProducts(search, category);
        List<Category> categories = productService.getAllCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("currentSearch", search != null ? search : "");
        model.addAttribute("currentCategory", category != null ? category : 0L);

        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "product-detail";
        }
        return "redirect:/products";
    }
}
