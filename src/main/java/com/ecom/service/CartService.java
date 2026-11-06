package com.ecom.service;
import com.ecom.exception.BusinessException;
import com.ecom.model.CartLine;
import com.ecom.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Service
public class CartService {
    public static final String SESSION_KEY="SMARTSHOP_CART";
    private final ProductService products;
    public CartService(ProductService products){this.products=products;}
    @SuppressWarnings("unchecked")
    public Map<Long,Integer> items(HttpSession session){
        Object obj=session.getAttribute(SESSION_KEY);
        if(obj instanceof Map<?,?>)return (Map<Long,Integer>)obj;
        Map<Long,Integer> cart=new LinkedHashMap<>(); session.setAttribute(SESSION_KEY,cart);return cart;
    }
    public void add(HttpSession session,long id,int amount){
        if(amount<1||amount>100)throw new BusinessException("Số lượng phải từ 1 đến 100");
        Product p=products.get(id);
        if(!p.active())throw new BusinessException("Sản phẩm ngừng kinh doanh");
        Map<Long,Integer> cart=items(session);
        int target=Math.addExact(cart.getOrDefault(id,0),amount);
        if(target>p.stock())throw new BusinessException("Chỉ còn "+p.stock()+" sản phẩm trong kho");
        cart.put(id,target);
    }
    public void update(HttpSession session,long id,int amount){
        if(amount<=0){items(session).remove(id);return;}
        Product p=products.get(id);
        if(!p.active()||amount>p.stock())throw new BusinessException("Sản phẩm không còn đủ số lượng");
        items(session).put(id,amount);
    }
    public List<CartLine> lines(HttpSession session){
        List<CartLine> result=new ArrayList<>();
        for(var item:items(session).entrySet()){
            Product product=products.get(item.getKey());
            result.add(new CartLine(product,item.getValue(),product.price().multiply(BigDecimal.valueOf(item.getValue()))));
        }
        return result;
    }
    public BigDecimal total(HttpSession session){return lines(session).stream().map(CartLine::subtotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
    public void clear(HttpSession session){items(session).clear();}
}
