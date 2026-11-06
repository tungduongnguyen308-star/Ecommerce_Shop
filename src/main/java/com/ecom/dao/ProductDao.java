package com.ecom.dao;
import com.ecom.model.Product;
import java.util.List;
import java.util.Optional;
public interface ProductDao {
    List<Product> findAll(String keyword, Long categoryId, boolean includeInactive);
    Optional<Product> findById(long id);
    long insert(Product product);
    boolean update(Product product);
    boolean delete(long id);
    boolean decreaseStock(long id, int quantity);
    void increaseStock(long id, int quantity);
    long count();
    long lowStockCount();
}
