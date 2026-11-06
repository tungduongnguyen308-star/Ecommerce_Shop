package com.ecom.config;

import com.ecom.dao.CategoryDao;
import com.ecom.service.StorefrontCategoryPolicy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * provide valid category IDs from MySQL to the header on every page.
 * No hard-coded database IDs; reuse the seven storefront categories of .
 */
@ControllerAdvice(annotations = Controller.class)
public class HeaderCategoryAdvice {
    private final CategoryDao categoryDao;

    public HeaderCategoryAdvice(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @ModelAttribute("headerCategories")
    public List<CategoryDao.CategoryRow> headerCategories() {
        return StorefrontCategoryPolicy.publicCategories(categoryDao.findAll());
    }
}
