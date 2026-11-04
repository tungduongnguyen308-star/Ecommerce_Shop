package com.ecom.service;
import com.ecom.dao.CategoryDao;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class FeaturedCategoryCatalogTest {
    @Test void twelveTilesHaveSevenRealCategoryIds() {
        var categories=List.of(
          new CategoryDao.CategoryRow(21L,"Điện thoại"), new CategoryDao.CategoryRow(30L,"Máy tính bảng"),
          new CategoryDao.CategoryRow(42L,"Laptop"),new CategoryDao.CategoryRow(57L,"Màn hình"),
          new CategoryDao.CategoryRow(89L,"PC - Máy tính để bàn"),new CategoryDao.CategoryRow(91L,"Phụ kiện"),
          new CategoryDao.CategoryRow(204L,"Điện máy"));
        var tiles=new FeaturedCategoryCatalog().tiles(categories);
        assertEquals(12,tiles.size());
        assertEquals("Máy tính bảng",tiles.get(1).name());
        assertEquals(30L,tiles.get(1).id());
        assertEquals("Phụ kiện",tiles.get(5).name());
        assertEquals("Máy xay sinh tố",tiles.get(8).name());
        assertEquals("Máy xay sinh tố",tiles.get(8).keyword());
        for (int i=6;i<12;i++) assertEquals(204L,tiles.get(i).id());
    }
    @Test void noDeadCategoryLinksWhenMissing(){
        assertEquals(0,new FeaturedCategoryCatalog().tiles(List.of()).size());
    }
}
