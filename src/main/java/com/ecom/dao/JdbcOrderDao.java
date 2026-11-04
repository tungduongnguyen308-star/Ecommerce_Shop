package com.ecom.dao;
import com.ecom.dto.CheckoutForm;
import com.ecom.model.OrderItem;
import com.ecom.model.OrderSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.Statement;
import java.util.List;
@Repository
public class JdbcOrderDao implements OrderDao {
    private final JdbcTemplate jdbc;
    public JdbcOrderDao(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override public long insertOrder(CheckoutForm form,BigDecimal total){
        KeyHolder key=new GeneratedKeyHolder();
        jdbc.update(con->{var ps=con.prepareStatement("INSERT INTO orders(customer_name,email,phone,address,note,total,status) VALUES(?,?,?,?,?,?,'PENDING')",Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,form.getCustomerName());ps.setString(2,form.getEmail());ps.setString(3,form.getPhone());ps.setString(4,form.getAddress());
            ps.setString(5,form.getNote());ps.setBigDecimal(6,total);return ps;},key);
        if(key.getKey()==null)throw new IllegalStateException("Không tạo được đơn hàng");
        return key.getKey().longValue();
    }
    @Override public void insertItem(long orderId,OrderItem item){
        jdbc.update("INSERT INTO order_items(order_id,product_id,product_name,quantity,unit_price,line_total) VALUES(?,?,?,?,?,?)",
                orderId,item.productId(),item.productName(),item.quantity(),item.unitPrice(),item.lineTotal());
    }
    @Override public List<OrderSummary> findAll(){return jdbc.query("SELECT id,customer_name,email,phone,address,total,status,created_at FROM orders ORDER BY id DESC",
            (r,i)->new OrderSummary(r.getLong("id"),r.getString("customer_name"),r.getString("email"),r.getString("phone"),
                    r.getString("address"),r.getBigDecimal("total"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime()));}
    @Override public List<OrderItem> findItems(long orderId){return jdbc.query("SELECT product_id,product_name,quantity,unit_price,line_total FROM order_items WHERE order_id=?",
            (r,i)->new OrderItem(r.getLong("product_id"),r.getString("product_name"),r.getInt("quantity"),r.getBigDecimal("unit_price"),r.getBigDecimal("line_total")),orderId);}
    @Override public String findStatusForUpdate(long orderId){
        var found=jdbc.query("SELECT status FROM orders WHERE id=? FOR UPDATE",(rs,i)->rs.getString("status"),orderId);
        return found.isEmpty()?null:found.get(0);
    }
    @Override public boolean updateStatus(long orderId,String status){return jdbc.update("UPDATE orders SET status=? WHERE id=?",status,orderId)>0;}
    @Override public long count(){return jdbc.queryForObject("SELECT COUNT(*) FROM orders",Long.class);}
    @Override public BigDecimal revenueCompleted(){return jdbc.queryForObject("SELECT COALESCE(SUM(total),0) FROM orders WHERE status='COMPLETED'",BigDecimal.class);}
}
