package com.ecom.service;
import com.ecom.dto.CheckoutForm;
import com.ecom.model.OrderSummary;
import com.ecom.model.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
public interface OrderService {
    long place(CheckoutForm customer,Map<Long,Integer> cart);
    List<OrderSummary> listAll();
    List<OrderSummary> listForCustomer(long customerUserId);
    Optional<OrderSummary> findForCustomer(long orderId,long customerUserId);
    List<OrderItem> getItems(long orderId);
    void setStatus(long orderId,String status);
    long count();
    BigDecimal completedRevenue();
}
