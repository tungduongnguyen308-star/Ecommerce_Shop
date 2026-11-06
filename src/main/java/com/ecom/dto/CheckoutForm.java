package com.ecom.dto;
import jakarta.validation.constraints.*;
public class CheckoutForm {
    @NotBlank @Size(max=150) private String customerName;
    @NotBlank @Email @Size(max=180) private String email;
    @NotBlank @Pattern(regexp="^[0-9+(). -]{9,20}$", message="Số điện thoại không hợp lệ") private String phone;
    @NotBlank @Size(min=8,max=350) private String address;
    @Size(max=500) private String note;
    private Long customerUserId;
    public String getCustomerName(){return customerName;} public void setCustomerName(String s){customerName=s;}
    public String getEmail(){return email;} public void setEmail(String s){email=s;}
    public String getPhone(){return phone;} public void setPhone(String s){phone=s;}
    public String getAddress(){return address;} public void setAddress(String s){address=s;}
    public String getNote(){return note;} public void setNote(String s){note=s;}
    public Long getCustomerUserId(){return customerUserId;} public void setCustomerUserId(Long id){customerUserId=id;}
}
