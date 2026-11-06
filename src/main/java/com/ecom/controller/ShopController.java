package com.ecom.controller;
import com.ecom.dao.CategoryDao;
import com.ecom.exception.BusinessException;
import com.ecom.service.ProductService;
import com.ecom.service.FeaturedCategoryCatalog;
import com.ecom.service.StorefrontCategoryPolicy;
import com.ecom.service.CatalogSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.Comparator;
@Controller
public class ShopController {
    private static final Logger log = LoggerFactory.getLogger(ShopController.class);
    private final ProductService products;
    private final CategoryDao categories;
    private final CatalogSearchService catalogSearch;
    private final FeaturedCategoryCatalog featuredCategoryCatalog;
    public ShopController(ProductService products, CategoryDao categories, CatalogSearchService catalogSearch, FeaturedCategoryCatalog featuredCategoryCatalog) {
        this.products=products;this.categories=categories;this.catalogSearch=catalogSearch;this.featuredCategoryCatalog=featuredCategoryCatalog;
    }
    @GetMapping("/") public String home(Model model){
        model.addAttribute("featured",products.browse(null,null,false).stream()
                .filter(StorefrontCategoryPolicy::visibleProduct).limit(8).toList());
        var allCategories=StorefrontCategoryPolicy.publicCategories(categories.findAll());
        model.addAttribute("categories",allCategories);
        model.addAttribute("featuredCategories",featuredCategoryCatalog.tiles(allCategories));
        return "index";
    }
    @GetMapping("/products") public String catalog(@RequestParam(required=false) String keyword,
            @RequestParam(required=false) Long categoryId,
            @RequestParam(defaultValue="mysql") String searchMode,
            @RequestParam(required=false) BigDecimal minPrice, @RequestParam(required=false) BigDecimal maxPrice,
            @RequestParam(defaultValue="default") String sort, Model model) {
        var found=products.browse(keyword,categoryId,false);
        String actualMode="mysql";
        if("qdrant".equals(searchMode) && keyword!=null && !keyword.isBlank()) {
            try {
                found=catalogSearch.search(keyword,categoryId);
                actualMode="qdrant";
            } catch (RuntimeException e) {
                log.warn("Qdrant unavailable; using JDBC keyword search: {}",e.getMessage());
                model.addAttribute("searchWarning","Qdrant chưa sẵn sàng hoặc chưa đồng bộ: đang dùng tìm kiếm MySQL.");
            }
        }
        found=found.stream().filter(StorefrontCategoryPolicy::visibleProduct)
                .filter(p -> minPrice==null || p.price().compareTo(minPrice)>=0)
                .filter(p -> maxPrice==null || p.price().compareTo(maxPrice)<=0).toList();
        Comparator<com.ecom.model.Product> comparator=switch(sort){
            case "priceAsc" -> Comparator.comparing(com.ecom.model.Product::price);
            case "priceDesc" -> Comparator.comparing(com.ecom.model.Product::price).reversed();
            case "nameAsc" -> Comparator.comparing(com.ecom.model.Product::name,String.CASE_INSENSITIVE_ORDER);
            default -> null;
        };
        if(comparator!=null) found=found.stream().sorted(comparator).toList();
        var visibleCategories=StorefrontCategoryPolicy.publicCategories(categories.findAll());
        model.addAttribute("products",found);model.addAttribute("categories",visibleCategories);
        model.addAttribute("keyword",keyword==null?"":keyword);model.addAttribute("categoryId",categoryId);
        model.addAttribute("searchMode",actualMode);model.addAttribute("minPrice",minPrice);model.addAttribute("maxPrice",maxPrice);model.addAttribute("sort",sort);
        String selectedCategoryName=visibleCategories.stream()
                .filter(cat -> categoryId != null && cat.id()==categoryId)
                .map(CategoryDao.CategoryRow::name).findFirst().orElse(null);
        model.addAttribute("selectedCategoryName",selectedCategoryName);
        return "products";
    }
    @GetMapping("/products/{id}") public String detail(@PathVariable long id,Model model){
        var p=products.get(id);if(!p.active() || !StorefrontCategoryPolicy.visibleProduct(p))
            throw new BusinessException("Sản phẩm không khả dụng");
        model.addAttribute("product",p);return "product-detail";
    }
    @GetMapping("/login") public String login(){return "login";}
}
