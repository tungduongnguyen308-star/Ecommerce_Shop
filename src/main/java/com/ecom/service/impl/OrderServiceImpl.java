package com.ecom.service.impl;
import com.ecom.dao.OrderDao;
import com.ecom.dao.ProductDao;
import com.ecom.dao.PromotionDao;
import com.ecom.dao.ProductPromotionDao;
import com.ecom.model.Promotion;
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
import java.util.Optional;
import java.util.Set;
@Service
public class OrderServiceImpl implements OrderService {
    private static final Set<String> ALLOWED=Set.of("PENDING","CONFIRMED","SHIPPED","COMPLETED","CANCELLED");
    private final ProductDao products;
    private final OrderDao orders;
    private final PromotionDao promotions;
    private final ProductPromotionDao productPromotions;
    @org.springframework.beans.factory.annotation.Autowired
    public OrderServiceImpl(ProductDao products,OrderDao orders,PromotionDao promotions,ProductPromotionDao productPromotions){this.products=products;this.orders=orders;this.promotions=promotions;this.productPromotions=productPromotions;}
    public OrderServiceImpl(ProductDao products,OrderDao orders){this.products=products;this.orders=orders;this.promotions=null;this.productPromotions=null;}
    public long place(CheckoutForm f,Map<Long,Integer> cart){return place(f,cart,null);}
    @Override @Transactional public long place(CheckoutForm f,Map<Long,Integer> cart,String couponCode){
        if(f==null||f.getCustomerName()==null||f.getCustomerName().isBlank()||f.getEmail()==null||!f.getEmail().contains("@")
                ||f.getPhone()==null||!f.getPhone().matches("^[0-9+(). -]{9,20}$")
                ||f.getAddress()==null||f.getAddress().isBlank()||f.getAddress().length()<8)
            throw new BusinessException("Thông tin người nhận chưa hợp lệ");
        if(cart==null||cart.isEmpty())throw new BusinessException("Giỏ hàng đang trống");
        List<OrderItem> lineItems=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;BigDecimal itemDiscountTotal=BigDecimal.ZERO;
        // Deterministic locking order minimizes deadlocks for concurrent orders.
        for(var e:cart.entrySet().stream().sorted(Comparator.comparingLong(Map.Entry::getKey)).toList()){
            if(e.getValue()==null||e.getValue()<1||e.getValue()>100)throw new BusinessException("Số lượng đặt hàng không hợp lệ");
            Product product=products.findById(e.getKey()).orElseThrow(()->new BusinessException("Sản phẩm không tồn tại"));
            if(!product.active())throw new BusinessException("Sản phẩm đã ngừng bán: "+product.name());
            if(!products.decreaseStock(product.id(),e.getValue()))throw new BusinessException("Không đủ hàng: "+product.name());
            BigDecimal original=product.price().multiply(BigDecimal.valueOf(e.getValue()));
            BigDecimal itemDiscount=productPromotions==null?BigDecimal.ZERO:productPromotions.discountFor(product.id(),product.price()).multiply(BigDecimal.valueOf(e.getValue()));
            BigDecimal discountedUnit=product.price().subtract(itemDiscount.divide(BigDecimal.valueOf(e.getValue()),2,java.math.RoundingMode.HALF_UP));
            BigDecimal subtotal=original.subtract(itemDiscount).max(BigDecimal.ZERO);
            lineItems.add(new OrderItem(product.id(),product.name(),e.getValue(),discountedUnit,subtotal));
            total=total.add(subtotal);itemDiscountTotal=itemDiscountTotal.add(itemDiscount);
        }
        BigDecimal couponDiscount=BigDecimal.ZERO;
        Promotion coupon=null;
        if(couponCode!=null&&!couponCode.isBlank()){
            if(promotions==null)throw new BusinessException("Không thể xác thực mã khuyến mãi");
            coupon=promotions.findUsableCode(couponCode);
            if(coupon==null)throw new BusinessException("Mã khuyến mãi không còn hợp lệ. Vui lòng áp dụng lại mã.");
            if(total.compareTo(coupon.getMinOrderValue())<0)throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu của mã khuyến mãi");
            couponDiscount="PERCENT".equals(coupon.getDiscountType())
                    ?total.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"),2,java.math.RoundingMode.HALF_UP)
                    :coupon.getDiscountValue();
            if(coupon.getMaxDiscount()!=null)couponDiscount=couponDiscount.min(coupon.getMaxDiscount());
            couponDiscount=couponDiscount.min(total).max(BigDecimal.ZERO);
        }
        BigDecimal payable=total.subtract(couponDiscount).max(BigDecimal.ZERO);
        long orderId=promotions==null?orders.insertOrder(f,payable):orders.insertOrder(f,payable,itemDiscountTotal.add(couponDiscount),couponCode);
        if(coupon!=null)promotions.incrementUsed(coupon.getId());
        for(OrderItem item:lineItems)orders.insertItem(orderId,item);
        // Any exception causes JDBC transaction rollback, including decremented inventory.
        return orderId;
    }
    @Override public List<OrderSummary> listAll(){return orders.findAll();}
    @Override public List<OrderSummary> listForCustomer(long customerUserId){return orders.findByCustomerUserId(customerUserId);}
    @Override public Optional<OrderSummary> findForCustomer(long orderId,long customerUserId){return orders.findByIdAndCustomerUserId(orderId,customerUserId);}
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
