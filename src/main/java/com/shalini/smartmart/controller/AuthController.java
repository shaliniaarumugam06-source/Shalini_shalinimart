package com.shalini.smartmart.controller;

import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.loginUser(email, password);
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole());

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/dashboard";
            }

            redirectAttributes.addFlashAttribute("success", "Welcome back, " + user.getName() + "!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
            redirectAttributes.addFlashAttribute("email", email != null ? email.trim() : "");
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/login")
    public String showAdminLoginForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        }
        // Transfer any interceptor flash error to model if present
        Object error = session.getAttribute("error");
        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("error");
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.loginUser(email, password);
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());
                return "redirect:/admin/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Access denied. Only administrator accounts can log in through the admin portal.");
                return "redirect:/admin/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid admin email or password.");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String address,
                           RedirectAttributes redirectAttributes) {
        // Validate password match
        if (password == null || !password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match. Please verify both passwords.");
            redirectAttributes.addFlashAttribute("name", name != null ? name.trim() : "");
            redirectAttributes.addFlashAttribute("email", email != null ? email.trim() : "");
            redirectAttributes.addFlashAttribute("phone", phone != null ? phone.trim() : "");
            redirectAttributes.addFlashAttribute("address", address != null ? address.trim() : "");
            return "redirect:/register";
        }

        try {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setPassword(password);
            newUser.setAddress(address);

            userService.registerCustomer(newUser);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login with your registered email and password.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("name", name != null ? name.trim() : "");
            redirectAttributes.addFlashAttribute("email", email != null ? email.trim() : "");
            redirectAttributes.addFlashAttribute("phone", phone != null ? phone.trim() : "");
            redirectAttributes.addFlashAttribute("address", address != null ? address.trim() : "");
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have logged out successfully.");
        return "redirect:/login";
    }
}
