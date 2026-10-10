package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.Order;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.service.OrderService;
import com.shalini.smartmart.service.ProductService;
import com.shalini.smartmart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private boolean checkAdminAccess(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Administrator privileges required.");
            return false;
        }
        return true;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        long totalProducts = productService.countProducts();
        long totalCustomers = userService.countCustomers();
        long totalOrders = orderService.countOrders();
        Double totalSales = orderService.getTotalSales();
        Double completedSales = orderService.getCompletedSales();
        long pendingOrders = orderService.getPendingOrdersCount();
        List<Product> lowStockProducts = productService.getLowStockProducts(5);
        List<Order> recentOrders = orderService.getAllOrders();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("completedSales", completedSales);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("lowStockCount", lowStockProducts.size());
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("recentOrders", recentOrders.size() > 5 ? recentOrders.subList(0, 5) : recentOrders);

        return "admin/dashboard";
    }

    // --- PRODUCT MANAGEMENT ---

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String search,
                               @RequestParam(required = false) Long category,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        List<Product> products = productService.searchProducts(search, category);
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("currentSearch", search != null ? search : "");
        model.addAttribute("currentCategory", category != null ? category : 0L);
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam Double price,
                              @RequestParam(defaultValue = "0.0") Double discount,
                              @RequestParam Integer stock,
                              @RequestParam String image,
                              @RequestParam Long categoryId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        try {
            Optional<Category> categoryOpt = productService.getCategoryById(categoryId);
            if (categoryOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid category selected.");
                return "redirect:/admin/products";
            }

            Product product;
            if (id != null && id > 0) {
                product = productService.getProductById(id).orElse(new Product());
            } else {
                product = new Product();
            }

            product.setName(name != null ? name.trim() : "");
            product.setDescription(description != null ? description.trim() : "");
            product.setPrice(price);
            product.setDiscount(discount != null ? discount : 0.0);
            product.setStock(stock);
            product.setImage(image != null ? image.trim() : "");
            product.setCategory(categoryOpt.get());

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product '" + product.getName() + "' saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            model.addAttribute("categories", productService.getAllCategories());
            return "admin/product-form";
        }
        redirectAttributes.addFlashAttribute("error", "Product not found.");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/stock")
    public String updateStock(@RequestParam Long productId,
                              @RequestParam Integer stock,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        try {
            productService.updateStock(productId, stock);
            redirectAttributes.addFlashAttribute("success", "Stock quantity updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update stock: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // --- ORDER MANAGEMENT ---

    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String status,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        List<Order> orders = orderService.searchAndFilterOrders(search, status);
        model.addAttribute("orders", orders);
        model.addAttribute("currentSearch", search != null ? search : "");
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("validStatuses", OrderService.VALID_STATUSES);
        return "admin/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("success", "Order " + updatedOrder.getOrderNumber() + " status updated to '" + status + "'.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // --- CUSTOMER MANAGEMENT ---

    @GetMapping("/customers")
    public String listCustomers(@RequestParam(required = false) String search,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        List<User> customers = userService.searchCustomers(search);
        Map<Long, Long> orderCountMap = new HashMap<>();
        for (User customer : customers) {
            long count = orderService.getOrderCountForUser(customer.getId());
            orderCountMap.put(customer.getId(), count);
        }

        model.addAttribute("customers", customers);
        model.addAttribute("orderCountMap", orderCountMap);
        model.addAttribute("currentSearch", search != null ? search : "");
        return "admin/customers";
    }

    @GetMapping("/customers/{id}")
    public String viewCustomerDetail(@PathVariable Long id,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty() || !"CUSTOMER".equalsIgnoreCase(userOpt.get().getRole())) {
            redirectAttributes.addFlashAttribute("error", "Customer not found.");
            return "redirect:/admin/customers";
        }

        User customer = userOpt.get();
        List<Order> orders = orderService.getOrdersForUser(customer);
        double totalSpent = 0.0;
        for (Order o : orders) {
            if (!"Cancelled".equalsIgnoreCase(o.getStatus())) {
                totalSpent += o.getTotalAmount();
            }
        }

        model.addAttribute("customer", customer);
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalSpent", Math.round(totalSpent * 100.0) / 100.0);

        return "admin/customer-detail";
    }

    // --- CATEGORY MANAGEMENT ---

    @GetMapping("/categories")
    public String listCategories(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String name,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        try {
            Category category;
            if (id != null && id > 0) {
                category = productService.getCategoryById(id).orElse(new Category());
            } else {
                category = new Category();
            }
            category.setName(name != null ? name.trim() : "");
            productService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category '" + category.getName() + "' saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save category: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        try {
            productService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // --- SALES REPORTS ---

    @GetMapping("/reports")
    public String salesReports(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/admin/login";
        }

        Double totalSales = orderService.getTotalSales();
        Double completedSales = orderService.getCompletedSales();
        Double cancelledSales = orderService.getCancelledSales();
        long totalOrders = orderService.countOrders();
        long pendingOrders = orderService.getPendingOrdersCount();

        long deliveredOrders = orderService.getOrderCountByStatus("Delivered");
        long cancelledOrders = orderService.getOrderCountByStatus("Cancelled");
        long placedOrders = orderService.getOrderCountByStatus("Order Placed");
        long confirmedOrders = orderService.getOrderCountByStatus("Confirmed");
        long packedOrders = orderService.getOrderCountByStatus("Packed");
        long outForDeliveryOrders = orderService.getOrderCountByStatus("Out for Delivery");

        List<Object[]> bestSellers = orderService.getBestSellingProducts();
        List<Order> recentOrders = orderService.getAllOrders();

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("completedSales", completedSales);
        model.addAttribute("cancelledSales", cancelledSales);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);

        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("placedOrders", placedOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("packedOrders", packedOrders);
        model.addAttribute("outForDeliveryOrders", outForDeliveryOrders);

        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("recentOrders", recentOrders.size() > 10 ? recentOrders.subList(0, 10) : recentOrders);

        return "admin/reports";
    }
}
