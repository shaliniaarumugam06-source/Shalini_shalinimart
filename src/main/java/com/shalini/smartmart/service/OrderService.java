package com.shalini.smartmart.service;

import com.shalini.smartmart.entity.*;
import com.shalini.smartmart.repository.CartRepository;
import com.shalini.smartmart.repository.OrderItemRepository;
import com.shalini.smartmart.repository.OrderRepository;
import com.shalini.smartmart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OrderService {

    public static final List<String> VALID_STATUSES = List.of(
            "Order Placed", "Confirmed", "Packed", "Out for Delivery", "Delivered", "Cancelled"
    );

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public Order placeOrder(User user, String deliveryAddress, String paymentMethod) throws Exception {
        Cart cart = cartService.getCartForUser(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new Exception("Your shopping cart is empty.");
        }

        // Check stock availability for all items before placing order
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStock()) {
                throw new Exception("Insufficient stock for product '" + product.getName() + "'. Available stock: " + product.getStock());
            }
        }

        // Generate unique Order Number (e.g., ORD-739204)
        String orderNumber = "ORD-" + (100000 + new Random().nextInt(900000));

        // Delivery Charge logic: Free if total >= 500, else 40
        double itemsTotal = cart.getTotalAmount();
        double deliveryCharge = itemsTotal >= 500.0 ? 0.0 : 40.0;
        double finalTotal = Math.round((itemsTotal + deliveryCharge) * 100.0) / 100.0;

        Order order = new Order(
                orderNumber,
                user,
                LocalDateTime.now(),
                finalTotal,
                "Order Placed",
                deliveryAddress != null && !deliveryAddress.isBlank() ? deliveryAddress.trim() : user.getAddress(),
                paymentMethod != null ? paymentMethod.trim() : "Cash on Delivery"
        );

        // Convert cart items to order items and deduct stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            // Deduct stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    item.getQuantity(),
                    product.getFinalPrice()
            );
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart items
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> searchAndFilterOrders(String keyword, String status) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim());

        if (hasKeyword && hasStatus) {
            return orderRepository.searchOrdersWithStatus(keyword.trim(), status.trim());
        } else if (hasKeyword) {
            return orderRepository.searchOrders(keyword.trim());
        } else if (hasStatus) {
            return orderRepository.findByStatusOrderByOrderDateDesc(status.trim());
        } else {
            return orderRepository.findAllByOrderByOrderDateDesc();
        }
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) throws Exception {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new Exception("Invalid order status: " + newStatus);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found."));

        String oldStatus = order.getStatus();
        if (oldStatus.equals(newStatus)) {
            return order;
        }

        // If transitioning to Cancelled from an active status, restore stock
        if ("Cancelled".equalsIgnoreCase(newStatus) && !"Cancelled".equalsIgnoreCase(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
        // If transitioning from Cancelled back to an active status, re-check and deduct stock
        else if (!"Cancelled".equalsIgnoreCase(newStatus) && "Cancelled".equalsIgnoreCase(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStock() < item.getQuantity()) {
                    throw new Exception("Cannot re-activate order. Product '" + product.getName() + "' has insufficient stock (Available: " + product.getStock() + ").");
                }
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public long countOrders() {
        return orderRepository.count();
    }

    public Double getTotalSales() {
        Double sales = orderRepository.calculateTotalSales();
        return sales != null ? Math.round(sales * 100.0) / 100.0 : 0.0;
    }

    public Double getCompletedSales() {
        Double sales = orderRepository.calculateCompletedSales();
        return sales != null ? Math.round(sales * 100.0) / 100.0 : 0.0;
    }

    public Double getCancelledSales() {
        Double sales = orderRepository.calculateCancelledSales();
        return sales != null ? Math.round(sales * 100.0) / 100.0 : 0.0;
    }

    public long getPendingOrdersCount() {
        return orderRepository.countPendingOrders();
    }

    public long getOrderCountByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    public long getOrderCountForUser(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    public List<Object[]> getBestSellingProducts() {
        return orderItemRepository.findBestSellingProducts();
    }
}
