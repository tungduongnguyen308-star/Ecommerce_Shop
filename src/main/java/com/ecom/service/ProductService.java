package com.ecom.service;
import com.ecom.dto.ProductForm;
import com.ecom.model.Product;
import java.util.List;
public interface ProductService {
    List<Product> browse(String keyword,Long categoryId,boolean admin);
    Product get(long id);
    long create(ProductForm form);
    void edit(long id,ProductForm form);
    void delete(long id);
    long count();
    long lowStockCount();
    ProductForm asForm(Product p);
}
