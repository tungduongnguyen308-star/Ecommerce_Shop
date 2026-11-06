package com.ecom.service;

import com.ecom.model.Product;
import java.util.List;

/** Tìm kiếm sản phẩm bằng Qdrant, không tạo hội thoại hoặc văn bản AI. */
public interface CatalogSearchService {
    int syncProducts();
    List<Product> search(String keyword, Long categoryId);
}
