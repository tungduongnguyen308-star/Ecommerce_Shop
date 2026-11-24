package com.ecom.service;
import com.ecom.dao.ProductPromotionDao;
import com.ecom.dao.PromotionDao;
import com.ecom.exception.BusinessException;
import com.ecom.model.CartLine;
import com.ecom.model.Product;
import com.ecom.model.Promotion;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
@Service
public class CartService {
    public static final String SESSION_KEY="SMARTSHOP_CART";
    private static final String COUPON_KEY="SMARTSHOP_COUPON";
    private final ProductService products;
    private final PromotionDao promotions;
    private final ProductPromotionDao productPromotions;
    public CartService(ProductService products, PromotionDao promotions, ProductPromotionDao productPromotions){this.products=products;this.promotions=promotions;this.productPromotions=productPromotions;}
    @SuppressWarnings("unchecked") public Map<Long,Integer> items(HttpSession session){
        Object obj=session.getAttribute(SESSION_KEY);
        if(obj instanceof Map<?,?> existing) {
            Map<Long,Integer> cart = new LinkedHashMap<>();
            existing.forEach((key, value) -> {
                if (key instanceof Number id && value instanceof Number quantity && quantity.intValue() > 0) {
                    cart.put(id.longValue(), quantity.intValue());
                }
            });
            // Chuẩn hóa kiểu khóa để tránh ClassCastException với session cũ.
            session.setAttribute(SESSION_KEY, cart);
            return cart;
        }
        Map<Long,Integer> cart=new LinkedHashMap<>();
        session.setAttribute(SESSION_KEY,cart);
        return cart;
    }
    public void add(HttpSession session,long id,int amount){
        if(amount<1||amount>100)throw new BusinessException("Số lượng phải từ 1 đến 100");
        Product p=products.get(id); if(!p.active())throw new BusinessException("Sản phẩm ngừng kinh doanh");
        Map<Long,Integer> cart=items(session); int target=Math.addExact(cart.getOrDefault(id,0),amount);
        if(target>p.stock())throw new BusinessException("Chỉ còn "+p.stock()+" sản phẩm trong kho"); cart.put(id,target); session.setAttribute(SESSION_KEY, cart);
    }
    public void update(HttpSession session,long id,int amount){
        if(amount<=0){Map<Long,Integer> cart=items(session); cart.remove(id); session.setAttribute(SESSION_KEY, cart); return;} Product p=products.get(id);
        if(!p.active()||amount>p.stock())throw new BusinessException("Sản phẩm không còn đủ số lượng"); Map<Long,Integer> cart=items(session); cart.put(id,amount); session.setAttribute(SESSION_KEY, cart);
    }
    public List<CartLine> lines(HttpSession session){
        List<CartLine> result=new ArrayList<>();
        for(var item:items(session).entrySet()){
            Product p=products.get(item.getKey()); int qty=item.getValue();
            BigDecimal original=p.price().multiply(BigDecimal.valueOf(qty));
            BigDecimal discount=productPromotions.discountFor(p.id(),p.price()).multiply(BigDecimal.valueOf(qty));
            result.add(new CartLine(p,qty,original.subtract(discount),discount));
        }
        return result;
    }
    public BigDecimal total(HttpSession session){return lines(session).stream().map(CartLine::subtotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
    public BigDecimal couponDiscount(HttpSession session){
        Promotion p=currentCoupon(session); if(p==null)return BigDecimal.ZERO;
        BigDecimal subtotal=total(session); if(subtotal.compareTo(p.getMinOrderValue())<0)return BigDecimal.ZERO;
        BigDecimal amount="PERCENT".equals(p.getDiscountType())?subtotal.multiply(p.getDiscountValue()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP):p.getDiscountValue();
        if(p.getMaxDiscount()!=null)amount=amount.min(p.getMaxDiscount()); return amount.min(subtotal).max(BigDecimal.ZERO);
    }
    public BigDecimal grandTotal(HttpSession session){return total(session).subtract(couponDiscount(session)).max(BigDecimal.ZERO);}
    public String couponCode(HttpSession session){Object v=session.getAttribute(COUPON_KEY);return v==null?null:v.toString();}
    public void applyCoupon(HttpSession session,String code){
        if(code==null||code.isBlank())throw new BusinessException("Vui lòng nhập mã khuyến mãi");
        Promotion p=promotions.findUsableCode(code);
        if(p==null)throw new BusinessException("Mã không tồn tại, đã hết hạn hoặc hết lượt sử dụng");
        if(total(session).compareTo(p.getMinOrderValue())<0)throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu để sử dụng mã");
        session.setAttribute(COUPON_KEY,p.getCode());
    }
    public void removeCoupon(HttpSession session){session.removeAttribute(COUPON_KEY);}
    public Promotion currentCoupon(HttpSession session){String code=couponCode(session);return code==null?null:promotions.findUsableCode(code);}
    public void clear(HttpSession session){Map<Long,Integer> cart=items(session); cart.clear(); session.setAttribute(SESSION_KEY, cart); removeCoupon(session);}
}
