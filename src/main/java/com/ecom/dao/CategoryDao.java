package com.ecom.dao;
import java.util.List;
public interface CategoryDao {
    List<CategoryRow> findAll();
    long create(String name);
    boolean delete(long id);
    record CategoryRow(long id,String name) { }
}
