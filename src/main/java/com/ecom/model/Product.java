package com.ecom.model;
import java.math.BigDecimal;
public record Product(long id, String name, String description, long categoryId,
                      String categoryName, BigDecimal price, int stock,
                      String imageUrl, boolean active) { }
