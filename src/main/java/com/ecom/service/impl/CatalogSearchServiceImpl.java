package com.ecom.service.impl;

import com.ecom.ai.QdrantGateway;
import com.ecom.exception.BusinessException;
import com.ecom.model.Product;
import com.ecom.service.CatalogSearchService;
import com.ecom.service.ProductService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogSearchServiceImpl implements CatalogSearchService {
    private final ProductService products;
    private final QdrantGateway qdrant;
    public CatalogSearchServiceImpl(ProductService products, QdrantGateway qdrant) {
        this.products=products;this.qdrant=qdrant;
    }
    @Override public int syncProducts() {
        return qdrant.sync(products.browse(null,null,false));
    }
    @Override public List<Product> search(String keyword, Long categoryId) {
        if (keyword==null || keyword.isBlank() || keyword.length()>200)
            throw new BusinessException("Nhập từ khóa từ 1 đến 200 ký tự để tìm bằng Qdrant");
        Map<Long,Product> visible = new LinkedHashMap<>();
        for (Product p : products.browse(null,categoryId,false)) if (p.stock()>0) visible.put(p.id(),p);
        List<Product> results = new ArrayList<>();
        for (Long id : qdrant.search(keyword,20)) {
            Product p = visible.get(id);
            if (p!=null && !results.contains(p)) results.add(p);
        }
        return results;
    }
}
