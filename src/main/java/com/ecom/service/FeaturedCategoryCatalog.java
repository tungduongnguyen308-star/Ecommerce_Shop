package com.ecom.service;

import com.ecom.dao.CategoryDao;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Twelve homepage tiles, mapped to seven actual MySQL categories. */
@Component
public class FeaturedCategoryCatalog {
    public record Tile(long id, String name, String image, String subtitle, String keyword) { }
    private record TileDef(String department, String name, String image, String keyword) { }

    private static final List<TileDef> TILES = List.of(
            tile("Điện thoại", "Điện thoại", "dien-thoai", null),
            tile("Máy tính bảng", "Máy tính bảng", "may-tinh-bang", null),
            tile("Laptop", "Laptop", "laptop", null),
            tile("Màn hình", "Màn hình", "man-hinh", null),
            tile("PC - Máy tính để bàn", "PC - Máy tính để bàn", "pc-may-tinh-de-ban", null),
            tile("Phụ kiện", "Phụ kiện", "phu-kien", null),
            tile("Điện máy", "Tivi", "tivi", "Tivi"),
            tile("Điện máy", "Máy giặt", "may-giat", "Máy giặt"),
            tile("Điện máy", "Máy xay sinh tố", "may-xay-sinh-to", "Máy xay sinh tố"),
            tile("Điện máy", "Robot hút bụi", "robot-hut-bui", "Robot hút bụi"),
            tile("Điện máy", "Tủ lạnh", "tu-lanh", "Tủ lạnh"),
            tile("Điện máy", "Máy lọc không khí", "may-loc-khong-khi", "Máy lọc không khí")
    );

    private static TileDef tile(String department, String label, String slug, String keyword) {
        return new TileDef(department, label, "/img/category-gallery/" + slug + ".webp", keyword);
    }

    public List<Tile> tiles(List<CategoryDao.CategoryRow> categories) {
        Map<String, Long> ids = new HashMap<>();
        for (var category : categories)
            ids.put(StorefrontCategoryPolicy.normalize(category.name()), category.id());
        List<Tile> result = new ArrayList<>();
        for (var definition : TILES) {
            Long id = ids.get(StorefrontCategoryPolicy.normalize(definition.department()));
            if (id != null)
                result.add(new Tile(id, definition.name(), definition.image(), definition.department(), definition.keyword()));
        }
        return List.copyOf(result);
    }
}
