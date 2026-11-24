package com.ecom.dao;

import com.ecom.model.Promotion;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.Date;
import java.util.List;

@Repository
public class PromotionDao {
    private final JdbcTemplate jdbc;
    public PromotionDao(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @PostConstruct public void initialize(){
        jdbc.execute("CREATE TABLE IF NOT EXISTS promotions (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(40) NOT NULL UNIQUE, name VARCHAR(180) NOT NULL, discount_type VARCHAR(12) NOT NULL, discount_value DECIMAL(14,2) NOT NULL, min_order_value DECIMAL(14,2) NOT NULL DEFAULT 0, max_discount DECIMAL(14,2) NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, usage_limit INT NULL, used_count INT NOT NULL DEFAULT 0, active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }
    private final RowMapper<Promotion> mapper=(r,n)->{Promotion p=new Promotion();p.setId(r.getLong("id"));p.setCode(r.getString("code"));p.setName(r.getString("name"));p.setDiscountType(r.getString("discount_type"));p.setDiscountValue(r.getBigDecimal("discount_value"));p.setMinOrderValue(r.getBigDecimal("min_order_value"));p.setMaxDiscount(r.getBigDecimal("max_discount"));p.setStartDate(r.getDate("start_date").toLocalDate());p.setEndDate(r.getDate("end_date").toLocalDate());p.setUsageLimit((Integer)r.getObject("usage_limit"));p.setUsedCount(r.getInt("used_count"));p.setActive(r.getBoolean("active"));return p;};
    public Promotion findUsableCode(String code){
        List<Promotion> found=jdbc.query("SELECT * FROM promotions WHERE UPPER(code)=? AND active=TRUE AND CURDATE() BETWEEN start_date AND end_date AND (usage_limit IS NULL OR used_count < usage_limit)",mapper,code.trim().toUpperCase());
        return found.isEmpty()?null:found.get(0);
    }
    public void incrementUsed(long id){jdbc.update("UPDATE promotions SET used_count=used_count+1 WHERE id=? AND (usage_limit IS NULL OR used_count < usage_limit)",id);}
    public List<Promotion> findAll(){return jdbc.query("SELECT * FROM promotions ORDER BY id DESC",mapper);}
    public Promotion find(long id){return jdbc.queryForObject("SELECT * FROM promotions WHERE id=?",mapper,id);}
    public void insert(Promotion p){jdbc.update("INSERT INTO promotions(code,name,discount_type,discount_value,min_order_value,max_discount,start_date,end_date,usage_limit,active) VALUES(?,?,?,?,?,?,?,?,?,?)",p.getCode(),p.getName(),p.getDiscountType(),p.getDiscountValue(),p.getMinOrderValue(),p.getMaxDiscount(),Date.valueOf(p.getStartDate()),Date.valueOf(p.getEndDate()),p.getUsageLimit(),p.isActive());}
    public void update(long id,Promotion p){jdbc.update("UPDATE promotions SET code=?,name=?,discount_type=?,discount_value=?,min_order_value=?,max_discount=?,start_date=?,end_date=?,usage_limit=?,active=? WHERE id=?",p.getCode(),p.getName(),p.getDiscountType(),p.getDiscountValue(),p.getMinOrderValue(),p.getMaxDiscount(),Date.valueOf(p.getStartDate()),Date.valueOf(p.getEndDate()),p.getUsageLimit(),p.isActive(),id);}
    public void delete(long id){jdbc.update("DELETE FROM promotions WHERE id=?",id);}
}
