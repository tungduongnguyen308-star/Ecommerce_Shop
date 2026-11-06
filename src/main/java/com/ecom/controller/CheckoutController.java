package com.ecom.controller;
import com.ecom.dto.CheckoutForm;
import com.ecom.exception.BusinessException;
import com.ecom.service.CartService;
import com.ecom.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
@Controller
public class CheckoutController {
    private final CartService cart; private final OrderService orders; private final JdbcTemplate jdbc;
    public CheckoutController(CartService cart,OrderService orders,JdbcTemplate jdbc){this.cart=cart;this.orders=orders;this.jdbc=jdbc;}
    @GetMapping("/checkout") public String form(HttpSession session,Model model,Authentication authentication){
        if(cart.items(session).isEmpty())return "redirect:/cart";
        if(!model.containsAttribute("checkoutForm")){
            CheckoutForm form=new CheckoutForm();
            if(authentication!=null){var r=jdbc.queryForMap("SELECT id,full_name,email,phone,address FROM app_users WHERE email=?",authentication.getName());
                form.setCustomerUserId(((Number)r.get("id")).longValue()); form.setCustomerName((String)r.get("full_name"));
                form.setEmail((String)r.get("email")); form.setPhone((String)r.get("phone")); form.setAddress((String)r.get("address"));}
            model.addAttribute("checkoutForm",form);
        }
        cartModel(session,model);return "checkout";
    }
    @PostMapping("/checkout") public String place(@Valid @ModelAttribute CheckoutForm checkoutForm,BindingResult errors,
                                                    HttpSession session,Model model,Authentication authentication){
        if(authentication!=null){Long userId=jdbc.queryForObject("SELECT id FROM app_users WHERE email=?",Long.class,authentication.getName());checkoutForm.setCustomerUserId(userId);}
        if(errors.hasErrors()){cartModel(session,model);return "checkout";}
        try{long id=orders.place(checkoutForm,cart.items(session));cart.clear(session);return "redirect:/order/success/"+id;}
        catch(BusinessException e){model.addAttribute("error",e.getMessage());cartModel(session,model);return "checkout";}
    }
    private void cartModel(HttpSession s,Model m){m.addAttribute("lines",cart.lines(s));m.addAttribute("total",cart.total(s));}
    @GetMapping("/order/success/{id}") public String success(@PathVariable long id,Model model){model.addAttribute("orderId",id);return "order-success";}
}
