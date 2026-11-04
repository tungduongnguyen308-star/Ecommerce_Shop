package com.ecom.controller;

import com.ecom.service.OrderService;
import com.ecom.model.OrderSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only list of customers extracted from actual orders. No new database schema. */
@Controller
@RequestMapping("/admin/customers")
public class AdminCustomersController {
    private final OrderService orderService;

    public AdminCustomersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        Map<String, CustomerRow> byCustomer = new LinkedHashMap<>();
        for (OrderSummary o : orderService.listAll()) {
            String email = o.email() == null ? "" : o.email().strip();
            String phone = o.phone() == null ? "" : o.phone().strip();
            String key = !email.isBlank() ? email.toLowerCase(java.util.Locale.ROOT)
                    : (!phone.isBlank() ? "phone:" + phone : "order:" + o.id());
            CustomerRow existing = byCustomer.get(key);
            if (existing == null) {
                byCustomer.put(key, new CustomerRow(o.customerName(), email, phone, 1, o.total()));
            } else {
                byCustomer.put(key, new CustomerRow(existing.name(), existing.email(),
                        existing.phone(), existing.orderCount() + 1,
                        existing.total().add(o.total())));
            }
        }
        model.addAttribute("customers", new ArrayList<>(byCustomer.values()));
        return "admin/customers";
    }

    public record CustomerRow(String name, String email, String phone,
                              long orderCount, BigDecimal total) { }
}
