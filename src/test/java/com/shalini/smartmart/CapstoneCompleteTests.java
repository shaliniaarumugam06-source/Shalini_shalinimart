package com.shalini.smartmart;

import com.shalini.smartmart.entity.Category;
import com.shalini.smartmart.entity.Order;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.repository.CategoryRepository;
import com.shalini.smartmart.repository.OrderRepository;
import com.shalini.smartmart.repository.ProductRepository;
import com.shalini.smartmart.repository.UserRepository;
import com.shalini.smartmart.service.CartService;
import com.shalini.smartmart.service.OrderService;
import com.shalini.smartmart.service.ProductService;
import com.shalini.smartmart.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order as TestOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CapstoneCompleteTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    private static String registeredCustomerEmail;
    private static Long registeredCustomerId;

    @Test
    @TestOrder(1)
    void testCustomerRegistration_Success() throws Exception {
        registeredCustomerEmail = "testreg" + System.currentTimeMillis() + "@example.com";

        mockMvc.perform(post("/register")
                .param("name", "Aarav Sharma")
                .param("email", registeredCustomerEmail)
                .param("phone", "9876543210")
                .param("password", "strongPass123")
                .param("confirmPassword", "strongPass123")
                .param("address", "Flat 401, Galaxy Residency, MG Road"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("success"));

        // Confirm customer saved in database
        User customer = userRepository.findByEmailIgnoreCase(registeredCustomerEmail).orElse(null);
        assertNotNull(customer);
        assertEquals("Aarav Sharma", customer.getName());
        assertEquals("CUSTOMER", customer.getRole());
        // Verify password is NOT stored in plain text
        assertNotEquals("strongPass123", customer.getPassword());
        assertTrue(customer.getPassword().contains(":"));

        registeredCustomerId = customer.getId();
    }

    @Test
    @TestOrder(2)
    void testCustomerRegistration_DuplicateEmailFails() throws Exception {
        // Attempt to register with the same email (even with different case)
        mockMvc.perform(post("/register")
                .param("name", "Duplicate Person")
                .param("email", registeredCustomerEmail.toUpperCase())
                .param("phone", "9876543211")
                .param("password", "anotherPass123")
                .param("confirmPassword", "anotherPass123")
                .param("address", "Somewhere Else"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("error", "This email address is already registered. Please login or use a different email."));
    }

    @Test
    @TestOrder(3)
    void testCustomerRegistration_ValidationErrors() throws Exception {
        // Password mismatch
        mockMvc.perform(post("/register")
                .param("name", "John Doe")
                .param("email", "john@valid.com")
                .param("phone", "9876543210")
                .param("password", "password123")
                .param("confirmPassword", "different123")
                .param("address", "123 Street"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("error"));

        // Invalid email format
        mockMvc.perform(post("/register")
                .param("name", "John Doe")
                .param("email", "invalid-email-format")
                .param("phone", "9876543210")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("address", "123 Street"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @TestOrder(4)
    void testCustomerLogin_SuccessAndRedirect() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                .session(session)
                .param("email", registeredCustomerEmail)
                .param("password", "strongPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("success"));

        // Verify session preserved
        User sessionUser = (User) session.getAttribute("user");
        assertNotNull(sessionUser);
        assertEquals(registeredCustomerEmail, sessionUser.getEmail());
        assertEquals("CUSTOMER", sessionUser.getRole());
    }

    @Test
    @TestOrder(5)
    void testCustomerLogin_WrongPassword() throws Exception {
        mockMvc.perform(post("/login")
                .param("email", registeredCustomerEmail)
                .param("password", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("error", "Invalid email or password."));
    }

    @Test
    @TestOrder(6)
    void testAdminLogin_Success() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();

        mockMvc.perform(post("/admin/login")
                .session(adminSession)
                .param("email", "admin@smartmart.com")
                .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        User adminUser = (User) adminSession.getAttribute("user");
        assertNotNull(adminUser);
        assertEquals("ADMIN", adminUser.getRole());
    }

    @Test
    @TestOrder(7)
    void testAdminRouteProtection_CustomerCannotAccessAdmin() throws Exception {
        // Customer session attempting to access admin dashboard
        User customer = userRepository.findByEmailIgnoreCase(registeredCustomerEmail).orElseThrow();
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("user", customer);
        customerSession.setAttribute("role", "CUSTOMER");

        mockMvc.perform(get("/admin/dashboard").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));

        // Unauthenticated guest attempting to access admin dashboard
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    @TestOrder(8)
    void testAdminProductManagement_CRUDAndValidation() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@smartmart.com").orElseThrow();
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("user", admin);
        adminSession.setAttribute("role", "ADMIN");

        Category category = categoryRepository.findAll().get(0);

        // 1. Add Product
        mockMvc.perform(post("/admin/products/save")
                .session(adminSession)
                .param("name", "Test Organic Green Tea (250g)")
                .param("description", "Pure green tea leaves rich in antioxidants.")
                .param("price", "220.00")
                .param("discount", "10.0")
                .param("stock", "30")
                .param("image", "https://images.unsplash.com/photo-1576092768241-dec231879fc3")
                .param("categoryId", String.valueOf(category.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("success"));

        // Verify saved
        List<Product> products = productRepository.findByNameContainingIgnoreCase("Test Organic Green Tea");
        assertFalse(products.isEmpty());
        Product p = products.get(0);
        assertEquals(30, p.getStock());

        // 2. Edit Product
        mockMvc.perform(post("/admin/products/save")
                .session(adminSession)
                .param("id", String.valueOf(p.getId()))
                .param("name", "Test Organic Green Tea (250g) Updated")
                .param("description", "Updated description.")
                .param("price", "240.00")
                .param("discount", "15.0")
                .param("stock", "45")
                .param("image", p.getImage())
                .param("categoryId", String.valueOf(category.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        Product updatedP = productRepository.findById(p.getId()).orElseThrow();
        assertEquals("Test Organic Green Tea (250g) Updated", updatedP.getName());
        assertEquals(45, updatedP.getStock());

        // 3. Update stock directly
        mockMvc.perform(post("/admin/products/stock")
                .session(adminSession)
                .param("productId", String.valueOf(p.getId()))
                .param("stock", "55"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("success"));

        Product stockP = productRepository.findById(p.getId()).orElseThrow();
        assertEquals(55, stockP.getStock());

        // 4. Reject negative stock
        mockMvc.perform(post("/admin/products/stock")
                .session(adminSession)
                .param("productId", String.valueOf(p.getId()))
                .param("stock", "-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("error"));

        // 5. Delete product
        mockMvc.perform(get("/admin/products/delete/" + p.getId())
                .session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("success"));

        assertTrue(productRepository.findById(p.getId()).isEmpty());
    }

    @Test
    @TestOrder(9)
    void testOrderPlacementStockDeductionAndStatusUpdate() throws Exception {
        User customer = userRepository.findByEmailIgnoreCase(registeredCustomerEmail).orElseThrow();
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("user", customer);
        customerSession.setAttribute("role", "CUSTOMER");

        // Get a sample product
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty());
        Product prod = products.get(0);
        int initialStock = prod.getStock();

        // 1. Add to cart
        mockMvc.perform(post("/cart/add")
                .session(customerSession)
                .param("productId", String.valueOf(prod.getId()))
                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // 2. Place order
        mockMvc.perform(post("/checkout")
                .session(customerSession)
                .param("deliveryAddress", "123 Main Street")
                .param("paymentMethod", "Cash on Delivery"))
                .andExpect(status().is3xxRedirection());

        // Verify stock deducted
        Product postOrderProd = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals(initialStock - 2, postOrderProd.getStock());

        // Verify customer has order
        List<Order> customerOrders = orderService.getOrdersForUser(customer);
        assertFalse(customerOrders.isEmpty());
        Order placedOrder = customerOrders.get(0);
        assertEquals("Order Placed", placedOrder.getStatus());

        // 3. Admin updates order status to "Confirmed", then "Delivered"
        User admin = userRepository.findByEmailIgnoreCase("admin@smartmart.com").orElseThrow();
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("user", admin);
        adminSession.setAttribute("role", "ADMIN");

        mockMvc.perform(post("/admin/orders/status")
                .session(adminSession)
                .param("orderId", String.valueOf(placedOrder.getId()))
                .param("status", "Delivered"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));

        Order deliveredOrder = orderRepository.findById(placedOrder.getId()).orElseThrow();
        assertEquals("Delivered", deliveredOrder.getStatus());

        // 4. Test status transition to "Cancelled" restores stock
        mockMvc.perform(post("/admin/orders/status")
                .session(adminSession)
                .param("orderId", String.valueOf(placedOrder.getId()))
                .param("status", "Cancelled"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));

        Product restoredProd = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals(initialStock, restoredProd.getStock());
    }

    @Test
    @TestOrder(10)
    void testDashboardAndSalesReportMetrics() throws Exception {
        User admin = userRepository.findByEmailIgnoreCase("admin@smartmart.com").orElseThrow();
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("user", admin);
        adminSession.setAttribute("role", "ADMIN");

        // Dashboard renders with attributes
        mockMvc.perform(get("/admin/dashboard").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("totalProducts", "totalCustomers", "totalOrders", "totalSales", "pendingOrders", "lowStockCount"));

        // Reports render with attributes
        mockMvc.perform(get("/admin/reports").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("totalSales", "completedSales", "totalOrders", "pendingOrders", "bestSellers"));
    }
}
