package com.ecom.dao;
import com.ecom.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
@Repository
public class JdbcProductDao implements ProductDao {
    private final JdbcTemplate jdbc;
    private static final String COLUMNS = "p.id,p.name,p.description,p.category_id,c.name AS category_name,p.price,p.stock,p.image_url,p.active";
    private static final RowMapper<Product> MAPPER = (rs,i)->new Product(
            rs.getLong("id"),rs.getString("name"),rs.getString("description"),rs.getLong("category_id"),
            rs.getString("category_name"),rs.getBigDecimal("price"),rs.getInt("stock"),
            rs.getString("image_url"),rs.getBoolean("active"));
    public JdbcProductDao(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override public List<Product> findAll(String keyword,Long categoryId,boolean includeInactive){
        // Binds all user parameters; never concatenates untrusted SQL.
        StringBuilder sql = new StringBuilder("SELECT "+COLUMNS+" FROM products p JOIN categories c ON c.id=p.category_id WHERE 1=1");
        List<Object> args = new java.util.ArrayList<>();
        if (!includeInactive) sql.append(" AND p.active=TRUE");
        if (keyword!=null&&!keyword.isBlank()) {
            sql.append(" AND (p.name COLLATE utf8mb4_unicode_ci LIKE ? OR p.description COLLATE utf8mb4_unicode_ci LIKE ?)");
            String term="%"+keyword.strip().toLowerCase(java.util.Locale.ROOT)+"%";
            args.add(term);args.add(term);
        }
        if (categoryId!=null) {sql.append(" AND p.category_id=?"); args.add(categoryId);}
        sql.append(" ORDER BY p.id DESC");
        return jdbc.query(sql.toString(),MAPPER,args.toArray());
    }
    @Override public Optional<Product> findById(long id){
        List<Product> found=jdbc.query("SELECT "+COLUMNS+" FROM products p JOIN categories c ON p.category_id=c.id WHERE p.id=?",MAPPER,id);
        return found.stream().findFirst();
    }
    @Override public long insert(Product p){
        KeyHolder keyHolder=new GeneratedKeyHolder();
        jdbc.update(con->{
            PreparedStatement ps=con.prepareStatement("INSERT INTO products(name,description,category_id,price,stock,image_url,active) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,p.name());ps.setString(2,p.description());ps.setLong(3,p.categoryId());
            ps.setBigDecimal(4,p.price());ps.setInt(5,p.stock());ps.setString(6,p.imageUrl());ps.setBoolean(7,p.active());
            return ps;
        },keyHolder);
        if(keyHolder.getKey()==null)throw new IllegalStateException("MySQL không trả về ID sản phẩm");
        return keyHolder.getKey().longValue();
    }
    @Override public boolean update(Product p){
        return jdbc.update("UPDATE products SET name=?,description=?,category_id=?,price=?,stock=?,image_url=?,active=? WHERE id=?",
                p.name(),p.description(),p.categoryId(),p.price(),p.stock(),p.imageUrl(),p.active(),p.id())>0;
    }
    @Override public boolean delete(long id){return jdbc.update("DELETE FROM products WHERE id=?",id)>0;}
    @Override public boolean decreaseStock(long id,int quantity){
        return jdbc.update("UPDATE products SET stock=stock-? WHERE id=? AND active=TRUE AND stock>=?",quantity,id,quantity)>0;
    }
    @Override public void increaseStock(long id,int quantity){
        if(quantity<=0)throw new IllegalArgumentException("Quantity must be positive");
        jdbc.update("UPDATE products SET stock=stock+? WHERE id=?",quantity,id);
    }
    @Override public long count(){return jdbc.queryForObject("SELECT COUNT(*) FROM products",Long.class);}
    @Override public long lowStockCount(){return jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE stock<=5",Long.class);}
}
