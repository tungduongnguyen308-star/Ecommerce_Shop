package com.ecom.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record OrderSummary(long id, String customerName, String email, String phone,
                           String address, BigDecimal total, String status, LocalDateTime createdAt) { }
