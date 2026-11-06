package com.ecom.controller;

import com.ecom.exception.BusinessException;
import com.ecom.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class CartController {
    private final CartService cart;
    public CartController(CartService cart) { this.cart = cart; }

    @GetMapping("/cart")
    public String view(HttpSession session, Model model) {
        model.addAttribute("lines", cart.lines(session));
        model.addAttribute("total", cart.total(session));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam long productId, @RequestParam(defaultValue = "1") int quantity,
                      HttpSession session, RedirectAttributes flash) {
        try {
            cart.add(session, productId, quantity);
            flash.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng");
        } catch (BusinessException | ArithmeticException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Dùng khi JavaScript bị tắt: nút x gửi quantity=0, biểu mẫu vẫn hoạt động.
    @PostMapping("/cart/update")
    public String update(@RequestParam long productId, @RequestParam int quantity,
                         HttpSession session, RedirectAttributes flash) {
        try {
            if (quantity < 0 || quantity > 100)
                throw new BusinessException("Số lượng phải từ 0 đến 100");
            cart.update(session, productId, quantity);
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // đồng bộ giỏ hàng trên server qua AJAX, giữ nguyên CSRF của Spring Security.
    @PostMapping(path = "/cart/api/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateLive(
            @RequestParam long productId, @RequestParam int quantity, HttpSession session) {
        if (quantity < 0 || quantity > 100)
            return ResponseEntity.badRequest().body(Map.<String, Object>of(
                    "ok", false, "message", "Số lượng phải từ 0 đến 100"));
        try {
            cart.update(session, productId, quantity);
            return ResponseEntity.ok(Map.<String, Object>of(
                    "ok", true, "quantity", quantity, "total", cart.total(session)));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.<String, Object>of(
                    "ok", false, "message", e.getMessage()));
        }
    }
}
