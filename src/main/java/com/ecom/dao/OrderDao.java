package com.ecom.dao;
import com.ecom.dto.CheckoutForm;
import com.ecom.model.OrderItem;
import com.ecom.model.OrderSummary;
import java.math.BigDecimal;
import java.util.List;
public interface OrderDao {
    long insertOrder(CheckoutForm customer,BigDecimal total);
    void insertItem(long orderId,OrderItem line);
    List<OrderSummary> findAll();
    List<OrderItem> findItems(long orderId);
    String findStatusForUpdate(long orderId);
    boolean updateStatus(long orderId,String status);
    long count();
    BigDecimal revenueCompleted();
}
