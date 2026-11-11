package com.ecom.controller;

import com.ecom.service.OrderService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account/orders")
public class CustomerOrderController {
    private final OrderService orders;
    private final JdbcTemplate jdbc;

    public CustomerOrderController(OrderService orders, JdbcTemplate jdbc) {
        this.orders = orders;
        this.jdbc = jdbc;
    }

    @GetMapping
    public String history(Authentication authentication, Model model) {
        long customerUserId = customerId(authentication);
        model.addAttribute("orders", orders.listForCustomer(customerUserId));
        return "account/order-history";
    }

    @GetMapping("/{orderId}")
    public String detail(@PathVariable long orderId, Authentication authentication, Model model) {
        long customerUserId = customerId(authentication);
        var order = orders.findForCustomer(orderId, customerUserId);
        if (order.isEmpty()) {
            return "redirect:/account/orders?notFound";
        }
        model.addAttribute("order", order.get());
        model.addAttribute("items", orders.getItems(orderId));
        return "account/order-detail";
    }

    private long customerId(Authentication authentication) {
        Long id = jdbc.queryForObject(
                "SELECT id FROM app_users WHERE email=? AND role='CUSTOMER' AND enabled=TRUE",
                Long.class, authentication.getName());
        if (id == null) {
            throw new IllegalStateException("Không tìm thấy tài khoản khách hàng đang đăng nhập");
        }
        return id;
    }
}
