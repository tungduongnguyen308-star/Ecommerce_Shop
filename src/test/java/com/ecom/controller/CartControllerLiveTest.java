package com.ecom.controller;

import com.ecom.exception.BusinessException;
import com.ecom.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerLiveTest {
    @Mock CartService cart;
    @Mock HttpSession session;
    @InjectMocks CartController controller;

    @Test
    void updateQuantityReturnsCurrentTotal() {
        when(cart.total(session)).thenReturn(new BigDecimal("13980000"));
        var result = controller.updateLive(5L, 2, session);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Boolean.TRUE, result.getBody().get("ok"));
        assertEquals(new BigDecimal("13980000"), result.getBody().get("total"));
        verify(cart).update(session, 5L, 2);
    }

    @Test
    void deleteItemUsesQuantityZero() {
        when(cart.total(session)).thenReturn(BigDecimal.ZERO);
        var result = controller.updateLive(5L, 0, session);
        assertEquals(200, result.getStatusCode().value());
        verify(cart).update(session, 5L, 0);
    }

    @Test
    void rejectsQuantityBeyondLimit() {
        var result = controller.updateLive(5L, 101, session);
        assertEquals(400, result.getStatusCode().value());
        verifyNoInteractions(cart);
    }

    @Test
    void reportsStockErrorWithoutUpdatingUi() {
        doThrow(new BusinessException("Sản phẩm không còn đủ số lượng"))
                .when(cart).update(session, 5L, 50);
        var result = controller.updateLive(5L, 50, session);
        assertEquals(400, result.getStatusCode().value());
        assertEquals(Boolean.FALSE, result.getBody().get("ok"));
        verify(cart, never()).total(session);
    }
}
