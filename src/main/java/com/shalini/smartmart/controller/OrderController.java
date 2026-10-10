package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.Cart;
import com.shalini.smartmart.entity.Order;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.service.CartService;
import com.shalini.smartmart.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null || !"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please login as customer to proceed to checkout.");
            return "redirect:/login";
        }

        Cart cart = cartService.getCartForUser(user);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty. Add products before checking out.");
            return "redirect:/cart";
        }

        double itemsSubtotal = cart.getTotalAmount();
        double deliveryCharge = (itemsSubtotal > 0 && itemsSubtotal < 500.0) ? 40.0 : 0.0;
        double finalTotal = Math.round((itemsSubtotal + deliveryCharge) * 100.0) / 100.0;

        model.addAttribute("cart", cart);
        model.addAttribute("itemsSubtotal", itemsSubtotal);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("user", user);

        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String deliveryAddress,
                             @RequestParam String paymentMethod,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null || !"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.placeOrder(user, deliveryAddress, paymentMethod);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully! Your Order ID is " + order.getOrderNumber());
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/orders")
    public String listMyOrders(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view your orders.");
            return "redirect:/login";
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/orders";
        }

        List<Order> orders = orderService.getOrdersForUser(user);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Ensure order belongs to current user or user is ADMIN
            if ("ADMIN".equalsIgnoreCase(user.getRole()) || order.getUser().getId().equals(user.getId())) {
                model.addAttribute("order", order);
                return "order-detail";
            }
        }

        redirectAttributes.addFlashAttribute("error", "Order not found or unauthorized.");
        return "redirect:" + ("ADMIN".equalsIgnoreCase(user.getRole()) ? "/admin/orders" : "/orders");
    }
}
