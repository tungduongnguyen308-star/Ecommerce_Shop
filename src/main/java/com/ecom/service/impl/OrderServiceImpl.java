package com.ecom.service.impl;
import com.ecom.dao.OrderDao;
import com.ecom.dao.ProductDao;
import com.ecom.dto.CheckoutForm;
import com.ecom.exception.BusinessException;
import com.ecom.model.OrderItem;
import com.ecom.model.OrderSummary;
import com.ecom.model.Product;
import com.ecom.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Service
public class OrderServiceImpl implements OrderService {
    private static final Set<String> ALLOWED=Set.of("PENDING","CONFIRMED","SHIPPED","COMPLETED","CANCELLED");
    private final ProductDao products;
    private final OrderDao orders;
    public OrderServiceImpl(ProductDao products,OrderDao orders){this.products=products;this.orders=orders;}
    @Override @Transactional public long place(CheckoutForm f,Map<Long,Integer> cart){
        if(f==null||f.getCustomerName()==null||f.getCustomerName().isBlank()||f.getEmail()==null||!f.getEmail().contains("@")
                ||f.getPhone()==null||!f.getPhone().matches("^[0-9+(). -]{9,20}$")
                ||f.getAddress()==null||f.getAddress().isBlank()||f.getAddress().length()<8)
            throw new BusinessException("Thông tin người nhận chưa hợp lệ");
        if(cart==null||cart.isEmpty())throw new BusinessException("Giỏ hàng đang trống");
        List<OrderItem> lineItems=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;
        // Deterministic locking order minimizes deadlocks for concurrent orders.
        for(var e:cart.entrySet().stream().sorted(Comparator.comparingLong(Map.Entry::getKey)).toList()){
            if(e.getValue()==null||e.getValue()<1||e.getValue()>100)throw new BusinessException("Số lượng đặt hàng không hợp lệ");
            Product product=products.findById(e.getKey()).orElseThrow(()->new BusinessException("Sản phẩm không tồn tại"));
            if(!product.active())throw new BusinessException("Sản phẩm đã ngừng bán: "+product.name());
            if(!products.decreaseStock(product.id(),e.getValue()))throw new BusinessException("Không đủ hàng: "+product.name());
            BigDecimal subtotal=product.price().multiply(BigDecimal.valueOf(e.getValue()));
            lineItems.add(new OrderItem(product.id(),product.name(),e.getValue(),product.price(),subtotal));total=total.add(subtotal);
        }
        long orderId=orders.insertOrder(f,total);
        for(OrderItem item:lineItems)orders.insertItem(orderId,item);
        // Any exception causes JDBC transaction rollback, including decremented inventory.
        return orderId;
    }
    @Override public List<OrderSummary> listAll(){return orders.findAll();}
    @Override public List<OrderItem> getItems(long orderId){return orders.findItems(orderId);}
    @Override @Transactional public void setStatus(long orderId,String status){
        if(!ALLOWED.contains(status))throw new BusinessException("Trạng thái đơn hàng không hợp lệ");
        String previous=orders.findStatusForUpdate(orderId);
        if(previous==null)throw new BusinessException("Không tìm thấy đơn hàng");
        if(previous.equals(status))return;
        boolean allowed=switch(previous){
            case "PENDING" -> status.equals("CONFIRMED")||status.equals("CANCELLED");
            case "CONFIRMED" -> status.equals("SHIPPED")||status.equals("CANCELLED");
            case "SHIPPED" -> status.equals("COMPLETED");
            default -> false;
        };
        if(!allowed)throw new BusinessException("Không thể chuyển từ "+previous+" sang "+status);
        if(status.equals("CANCELLED")){
            for(OrderItem item:orders.findItems(orderId)){
                // A deleted product is preserved as a name/price snapshot in order_items.
                if(item.productId()>0)products.increaseStock(item.productId(),item.quantity());
            }
        }
        if(!orders.updateStatus(orderId,status))throw new BusinessException("Không cập nhật được đơn hàng");
    }
    @Override public long count(){return orders.count();}
    @Override public BigDecimal completedRevenue(){return orders.revenueCompleted();}
}
