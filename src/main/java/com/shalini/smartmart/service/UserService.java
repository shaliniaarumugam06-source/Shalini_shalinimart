package com.shalini.smartmart.service;

import com.shalini.smartmart.entity.Cart;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.repository.CartRepository;
import com.shalini.smartmart.repository.UserRepository;
import com.shalini.smartmart.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    public User registerCustomer(User user) throws Exception {
        // Validate inputs
        if (user.getName() == null || user.getName().trim().length() < 2) {
            throw new Exception("Full Name is required (at least 2 characters).");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new Exception("Email Address is required.");
        }

        String normalizedEmail = user.getEmail().trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new Exception("Please enter a valid email address (e.g., name@example.com).");
        }

        if (user.getPhone() == null || user.getPhone().trim().replaceAll("[^0-9]", "").length() < 10) {
            throw new Exception("Please enter a valid 10-digit phone number.");
        }

        if (user.getPassword() == null || user.getPassword().trim().length() < 6) {
            throw new Exception("Password must be at least 6 characters long.");
        }

        if (user.getAddress() == null || user.getAddress().trim().length() < 5) {
            throw new Exception("Delivery address is required (at least 5 characters).");
        }

        // Email uniqueness check (case-insensitive)
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new Exception("This email address is already registered. Please login or use a different email.");
        }

        user.setName(user.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(user.getPhone().trim());
        user.setAddress(user.getAddress().trim());
        user.setPassword(PasswordUtil.hashPassword(user.getPassword().trim()));
        user.setRole("CUSTOMER");

        User savedUser = userRepository.save(user);

        // Automatically initialize an empty shopping cart for the new customer
        Cart cart = new Cart(savedUser);
        cartRepository.save(cart);

        return savedUser;
    }

    public User createAdminIfNotExists(String name, String email, String password, String phone, String address) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> existing = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isPresent()) {
            User admin = existing.get();
            // Ensure password is securely hashed if still stored in plain text
            if (!admin.getPassword().contains(":")) {
                admin.setPassword(PasswordUtil.hashPassword(password));
                admin.setRole("ADMIN");
                return userRepository.save(admin);
            }
            return admin;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User admin = new User(name.trim(), normalizedEmail, hashedPassword, phone.trim(), address.trim(), "ADMIN");
        return userRepository.save(admin);
    }

    public User loginUser(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new Exception("Email and password cannot be empty.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(normalizedEmail);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(password.trim(), user.getPassword())) {
                // If password was stored in plain text, upgrade it to salted hash
                if (!user.getPassword().contains(":")) {
                    user.setPassword(PasswordUtil.hashPassword(password.trim()));
                    userRepository.save(user);
                }
                return user;
            }
        }

        throw new Exception("Invalid email or password.");
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateProfile(Long id, String name, String phone, String address) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found"));

        if (name == null || name.trim().length() < 2) {
            throw new Exception("Name must be at least 2 characters.");
        }
        if (phone == null || phone.trim().replaceAll("[^0-9]", "").length() < 10) {
            throw new Exception("Please provide a valid 10-digit phone number.");
        }
        if (address == null || address.trim().length() < 5) {
            throw new Exception("Address must be at least 5 characters.");
        }

        user.setName(name.trim());
        user.setPhone(phone.trim());
        user.setAddress(address.trim());
        return userRepository.save(user);
    }

    public List<User> getAllCustomers() {
        return userRepository.findByRole("CUSTOMER");
    }

    public List<User> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return userRepository.searchCustomers("CUSTOMER", keyword.trim());
    }

    public long countCustomers() {
        return userRepository.countByRole("CUSTOMER");
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
