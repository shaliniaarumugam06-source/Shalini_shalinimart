package com.shalini.smartmart.config;

import com.shalini.smartmart.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow public admin login endpoints
        if (uri.equals("/admin/login") || uri.startsWith("/admin/login/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            if (session != null) {
                session.setAttribute("error", "Access denied. Administrator privileges are required to view this page.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return false;
        }

        return true;
    }
}
