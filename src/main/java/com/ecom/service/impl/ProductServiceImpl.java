package com.ecom.service.impl;

import com.ecom.dao.ProductDao;
import com.ecom.dto.ProductForm;
import com.ecom.exception.BusinessException;
import com.ecom.model.Product;
import com.ecom.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductDao dao;

    public ProductServiceImpl(ProductDao dao) {
        this.dao = dao;
    }

    @Override
    public List<Product> browse(String keyword, Long categoryId, boolean admin) {
        return dao.findAll(keyword, categoryId, admin);
    }

    @Override
    public Product get(long id) {
        return dao.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm #" + id));
    }

    @Override
    @Transactional
    public long create(ProductForm f) {
        validate(f);
        return dao.insert(toProduct(0, f));
    }

    @Override
    @Transactional
    public void edit(long id, ProductForm f) {
        validate(f);
        get(id);
        if (!dao.update(toProduct(id, f))) throw new BusinessException("Cập nhật không thành công");
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!dao.delete(id)) throw new BusinessException("Không tìm thấy sản phẩm");
    }

    @Override
    public long count() {
        return dao.count();
    }

    @Override
    public long lowStockCount() {
        return dao.lowStockCount();
    }

    private static void validate(ProductForm f) {
        if (f.getName() == null || f.getName().isBlank()) throw new BusinessException("Tên sản phẩm không được trống");
        if (f.getDescription() == null || f.getDescription().isBlank())
            throw new BusinessException("Mô tả không được trống");
        if (f.getCategoryId() == null || f.getCategoryId() < 1) throw new BusinessException("Danh mục không hợp lệ");
        if (f.getPrice() == null || f.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("Giá phải lớn hơn 0");
        if (f.getStock() == null || f.getStock() < 0) throw new BusinessException("Tồn kho phải từ 0 trở lên");
        if (f.getImageUrl() == null || f.getImageUrl().isBlank() || !(f.getImageUrl().startsWith("/img/") || f.getImageUrl().startsWith("https://")))
            throw new BusinessException("Ảnh phải là đường dẫn /img/ hoặc https://");
    }

    private static Product toProduct(long id, ProductForm f) {
        return new Product(id, f.getName().strip(), f.getDescription().strip(), f.getCategoryId(), "", f.getPrice(), f.getStock(), f.getImageUrl().strip(), f.isActive());
    }

    @Override
    public ProductForm asForm(Product p) {
        var f = new ProductForm();
        f.setName(p.name());
        f.setDescription(p.description());
        f.setCategoryId(p.categoryId());
        f.setPrice(p.price());
        f.setStock(p.stock());
        f.setImageUrl(p.imageUrl());
        f.setActive(p.active());
        return f;
    }
}
