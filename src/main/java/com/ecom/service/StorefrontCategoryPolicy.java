package com.ecom.service;

import com.ecom.dao.CategoryDao;
import com.ecom.model.Product;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** seven searchable categories, without exposing legacy fashion. */
public final class StorefrontCategoryPolicy {
    private StorefrontCategoryPolicy() { }
    private static final List<String> ALLOWED_ORDER = List.of(
            "dien thoai", "may tinh bang", "laptop", "man hinh",
            "pc may tinh de ban", "phu kien", "dien may");
    private static final Set<String> FASHION_TERMS = Set.of(
            "giay", "dep", "quan", "ao", "vay", "my pham", "nuoc hoa");

    public static String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ")
                .trim().replaceAll(" +", " ");
    }

    public static boolean visibleCategory(String name) {
        return ALLOWED_ORDER.contains(normalize(name));
    }

    public static boolean visibleProduct(Product product) {
        if (product == null || !visibleCategory(product.categoryName())) return false;
        String name = " " + normalize(product.name()) + " ";
        for (String term : FASHION_TERMS)
            if (name.contains(" " + term + " ")) return false;
        return true;
    }

    public static List<CategoryDao.CategoryRow> publicCategories(List<CategoryDao.CategoryRow> categories) {
        return categories.stream().filter(c -> visibleCategory(c.name()))
                .sorted((a, b) -> Integer.compare(
                        ALLOWED_ORDER.indexOf(normalize(a.name())),
                        ALLOWED_ORDER.indexOf(normalize(b.name()))))
                .toList();
    }
}
