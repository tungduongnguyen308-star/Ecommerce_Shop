package com.ecom.dao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.util.List;
@Repository
public class JdbcCategoryDao implements CategoryDao {
    private final JdbcTemplate jdbc;
    public JdbcCategoryDao(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override public List<CategoryRow> findAll(){return jdbc.query("SELECT id,name FROM categories ORDER BY name",(r,i)->new CategoryRow(r.getLong("id"),r.getString("name")));}
    @Override public long create(String name){
        KeyHolder kh=new GeneratedKeyHolder();
        jdbc.update(con->{var p=con.prepareStatement("INSERT INTO categories(name) VALUES(?)",Statement.RETURN_GENERATED_KEYS);p.setString(1,name);return p;},kh);
        return kh.getKey().longValue();
    }
    @Override public boolean delete(long id){return jdbc.update("DELETE FROM categories WHERE id=?",id)>0;}
}
