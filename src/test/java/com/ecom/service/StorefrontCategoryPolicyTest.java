package com.ecom.service;
import com.ecom.dao.CategoryDao;
import com.ecom.model.Product;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class StorefrontCategoryPolicyTest {
    private static Product product(String name,String category){
        return new Product(1L,name,"Mô tả",7L,category,new BigDecimal("100000"),2,"/img/demo.webp",true);
    }
    @Test void sevenFilterCategoriesAndNoFashion(){
        var categories=List.of(new CategoryDao.CategoryRow(1L,"Điện máy"),
          new CategoryDao.CategoryRow(2L,"Máy tính bảng"),new CategoryDao.CategoryRow(3L,"Điện thoại"),
          new CategoryDao.CategoryRow(4L,"Laptop"),new CategoryDao.CategoryRow(5L,"Phụ kiện"),
          new CategoryDao.CategoryRow(6L,"Màn hình"),new CategoryDao.CategoryRow(7L,"PC - Máy tính để bàn"),
          new CategoryDao.CategoryRow(8L,"Giày dép"));
        assertEquals(List.of("Điện thoại","Máy tính bảng","Laptop","Màn hình","PC - Máy tính để bàn","Phụ kiện","Điện máy"),
          StorefrontCategoryPolicy.publicCategories(categories).stream().map(CategoryDao.CategoryRow::name).toList());
    }
    @Test void appliancesBelongToElectricalAndShoesStayHidden(){
        assertTrue(StorefrontCategoryPolicy.visibleProduct(product("Robot hút bụi thông minh","Điện máy")));
        assertTrue(StorefrontCategoryPolicy.visibleProduct(product("Máy xay sinh tố gia đình","Điện máy")));
        assertTrue(StorefrontCategoryPolicy.visibleProduct(product("Máy lọc không khí","Điện máy")));
        assertTrue(StorefrontCategoryPolicy.visibleProduct(product("Máy tính bảng Samsung","Máy tính bảng")));
        assertFalse(StorefrontCategoryPolicy.visibleProduct(product("Giày thể thao","Phụ kiện")));
        assertFalse(StorefrontCategoryPolicy.visibleProduct(product("Áo sơ mi","Điện máy")));
    }
}
