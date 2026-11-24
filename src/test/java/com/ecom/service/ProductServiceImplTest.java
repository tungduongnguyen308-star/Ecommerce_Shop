package com.ecom.service;

import com.ecom.dao.ProductDao;
import com.ecom.dto.ProductForm;
import com.ecom.exception.BusinessException;
import com.ecom.model.Product;
import com.ecom.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {
    private ProductDao dao;
    private ProductServiceImpl service;

    @BeforeEach void init() {dao=mock(ProductDao.class);service=new ProductServiceImpl(dao);}
    private static ProductForm validForm() {
        ProductForm f=new ProductForm();
        f.setName("Laptop demo");f.setDescription("Máy tính học tập");f.setCategoryId(1L);
        f.setPrice(new BigDecimal("12000000"));f.setStock(5);f.setImageUrl("/img/ecom.png");
        f.setActive(true);return f;
    }
    @Test void createValidProductCallsDao() {
        when(dao.insert(any(Product.class))).thenReturn(21L);
        assertEquals(21L,service.create(validForm()));
        verify(dao).insert(argThat(p -> p.name().equals("Laptop demo") && p.stock()==5));
    }
    @Test void rejectNegativePriceBeforeSaving() {
        ProductForm f=validForm();f.setPrice(new BigDecimal("-1"));
        assertThrows(BusinessException.class,()->service.create(f));
        verifyNoInteractions(dao);
    }
    @Test void updateProductCallsJdbcDao() {
        ProductForm f=validForm();
        when(dao.findById(7L)).thenReturn(Optional.of(new Product(7L,"Old","Old",1L,"Laptop",new BigDecimal("100"),2,"/img/ecom.png",true)));
        when(dao.update(any(Product.class))).thenReturn(true);
        service.edit(7L,f);
        verify(dao).update(argThat(p->p.id()==7L && p.price().compareTo(new BigDecimal("12000000"))==0));
    }
    @Test void deleteProductCallsDao() {
        when(dao.delete(7L)).thenReturn(true);
        service.delete(7L);
        verify(dao).delete(7L);
    }
}
