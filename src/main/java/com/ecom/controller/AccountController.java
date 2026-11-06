package com.ecom.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
    public AccountController(JdbcTemplate jdbc, PasswordEncoder encoder){this.jdbc=jdbc;this.encoder=encoder;}
    @GetMapping("/register") public String register(Model model){if(!model.containsAttribute("form"))model.addAttribute("form",new RegisterForm());return "register";}
    @PostMapping("/register") public String create(@Valid @ModelAttribute("form") RegisterForm form, BindingResult errors, Model model){
        if(!java.util.Objects.equals(form.getPassword(),form.getConfirmPassword())) errors.rejectValue("confirmPassword","mismatch","Mật khẩu xác nhận không khớp.");
        if(!errors.hasErrors()) {
            Integer exists=jdbc.queryForObject("SELECT COUNT(*) FROM app_users WHERE email=?",Integer.class,form.getEmail().trim().toLowerCase());
            if(exists!=null && exists>0) errors.rejectValue("email","duplicate","Email đã được đăng ký.");
        }
        if(errors.hasErrors()) return "register";
        jdbc.update("INSERT INTO app_users(email,password_hash,role,enabled,full_name,phone,address) VALUES(?,?,'CUSTOMER',TRUE,?,?,?)",
                form.getEmail().trim().toLowerCase(),encoder.encode(form.getPassword()),form.getFullName().trim(),form.getPhone().trim(),form.getAddress().trim());
        return "redirect:/login?registered";
    }
    public static class RegisterForm {
        @NotBlank @Size(max=150) private String fullName;
        @NotBlank @Email @Size(max=180) private String email;
        @NotBlank @Pattern(regexp="^[0-9+(). -]{9,20}$",message="Số điện thoại không hợp lệ") private String phone;
        @NotBlank @Size(min=8,max=350) private String address;
        @NotBlank @Size(min=8,max=72,message="Mật khẩu phải có ít nhất 8 ký tự") private String password;
        @NotBlank private String confirmPassword;
        public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
        public String getEmail(){return email;} public void setEmail(String v){email=v;}
        public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
        public String getAddress(){return address;} public void setAddress(String v){address=v;}
        public String getPassword(){return password;} public void setPassword(String v){password=v;}
        public String getConfirmPassword(){return confirmPassword;} public void setConfirmPassword(String v){confirmPassword=v;}
    }
}
