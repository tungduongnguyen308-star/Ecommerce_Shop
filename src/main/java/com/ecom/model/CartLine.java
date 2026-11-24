package com.ecom.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CartLine(Product product, int quantity, BigDecimal subtotal, BigDecimal discountAmount) {
    public BigDecimal getOriginalSubtotal() {
        return product.price().multiply(BigDecimal.valueOf(quantity));
    }

    /** Giá sau khuyến mãi trên một sản phẩm, dùng cho giao diện giỏ hàng. */
    public BigDecimal getUnitPrice() {
        if (quantity <= 0) return BigDecimal.ZERO;
        return subtotal.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
    }
}
