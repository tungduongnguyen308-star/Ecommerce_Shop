package com.ecom.service;

import com.ecom.dao.OrderDao;
import com.ecom.dao.ProductDao;
import com.ecom.dto.CheckoutForm;
import com.ecom.exception.BusinessException;
import com.ecom.model.OrderItem;
import com.ecom.model.Product;
import com.ecom.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {
    private ProductDao products;
    private OrderDao orders;
    private OrderServiceImpl service;
    private final Product laptop=new Product(1L,"Laptop A","RAM 16GB",1L,"Laptop",new BigDecimal("15000000"),5,"/img/ecom.png",true);
    @BeforeEach void init(){products=mock(ProductDao.class);orders=mock(OrderDao.class);service=new OrderServiceImpl(products,orders);}
    private CheckoutForm customer(){
        var f=new CheckoutForm();f.setCustomerName("Nguyen Van A");f.setEmail("test@example.com");
        f.setPhone("0912345678");f.setAddress("Ha Noi, Viet Nam");return f;
    }
    @Test void placeOrderComputesCorrectTotalAndDecreasesStock() {
        when(products.findById(1L)).thenReturn(Optional.of(laptop));
        when(products.decreaseStock(1L,2)).thenReturn(true);
        when(orders.insertOrder(any(),eq(new BigDecimal("30000000")))).thenReturn(25L);
        assertEquals(25L,service.place(customer(),Map.of(1L,2)));
        verify(products).decreaseStock(1L,2);
        verify(orders).insertItem(eq(25L),argThat(i->i.quantity()==2 && i.lineTotal().compareTo(new BigDecimal("30000000"))==0));
    }
    @Test void customerHistoryIsScopedToTheRequestedCustomer(){
        when(orders.findByCustomerUserId(42L)).thenReturn(List.of());
        assertTrue(service.listForCustomer(42L).isEmpty());
        verify(orders).findByCustomerUserId(42L);
    }
    @Test void customerOrderDetailUsesBothOrderAndCustomerIds(){
        when(orders.findByIdAndCustomerUserId(25L,42L)).thenReturn(Optional.empty());
        assertTrue(service.findForCustomer(25L,42L).isEmpty());
        verify(orders).findByIdAndCustomerUserId(25L,42L);
    }
    @Test void emptyCartIsRejectedWithoutDatabaseWrites(){
        assertThrows(BusinessException.class,()->service.place(customer(),Map.of()));
        verifyNoInteractions(products,orders);
    }
    @Test void insufficientStockIsRejectedWithoutInsertingOrder(){
        when(products.findById(1L)).thenReturn(Optional.of(laptop));
        when(products.decreaseStock(1L,9)).thenReturn(false);
        assertThrows(BusinessException.class,()->service.place(customer(),Map.of(1L,9)));
        verify(orders,never()).insertOrder(any(),any());
    }
    @Test void cancelConfirmedOrderRestoresInventory() {
        when(orders.findStatusForUpdate(9L)).thenReturn("CONFIRMED");
        when(orders.findItems(9L)).thenReturn(List.of(new OrderItem(1L,"Laptop A",2,new BigDecimal("15000000"),new BigDecimal("30000000"))));
        when(orders.updateStatus(9L,"CANCELLED")).thenReturn(true);
        service.setStatus(9L,"CANCELLED");
        verify(products).increaseStock(1L,2);
        verify(orders).updateStatus(9L,"CANCELLED");
    }
    @Test void cannotCancelShippedOrder(){
        when(orders.findStatusForUpdate(9L)).thenReturn("SHIPPED");
        assertThrows(BusinessException.class,()->service.setStatus(9L,"CANCELLED"));
        verify(products,never()).increaseStock(anyLong(),anyInt());
    }
}
