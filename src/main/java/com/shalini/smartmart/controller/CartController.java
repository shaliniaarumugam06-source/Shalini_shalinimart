package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.Cart;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    private User getLoggedInUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && "CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return user;
        }
        return null;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login as customer to access your shopping cart.");
            return "redirect:/login";
        }

        Cart cart = cartService.getCartForUser(user);
        double itemsSubtotal = cart.getTotalAmount();
        double deliveryCharge = (itemsSubtotal > 0 && itemsSubtotal < 500.0) ? 40.0 : 0.0;
        double finalTotal = Math.round((itemsSubtotal + deliveryCharge) * 100.0) / 100.0;

        model.addAttribute("cart", cart);
        model.addAttribute("itemsSubtotal", itemsSubtotal);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("finalTotal", finalTotal);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to add items to your cart.");
            return "redirect:/login";
        }

        try {
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Item added to your cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateQuantity(user, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Long cartItemId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeFromCart(user, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }
}
