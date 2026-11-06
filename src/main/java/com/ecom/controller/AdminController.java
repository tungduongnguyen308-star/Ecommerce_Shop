package com.ecom.controller;
import com.ecom.dao.CategoryDao;
import com.ecom.dto.ProductForm;
import com.ecom.exception.BusinessException;
import com.ecom.service.CatalogSearchService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.StorefrontCategoryPolicy;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductService products;
    private final OrderService orders;
    private final CategoryDao categories;
    private final CatalogSearchService catalogSearch;
    public AdminController(ProductService products,OrderService orders,CategoryDao categories,CatalogSearchService catalogSearch){
        this.products=products;this.orders=orders;this.categories=categories;this.catalogSearch=catalogSearch;
    }
    @GetMapping public String dashboard(Model model){
        model.addAttribute("productCount",products.browse(null,null,true).stream()
                .filter(StorefrontCategoryPolicy::visibleProduct).count());model.addAttribute("orderCount",orders.count());
        model.addAttribute("lowStockCount",products.lowStockCount());model.addAttribute("revenue",orders.completedRevenue());
        model.addAttribute("orders",orders.listAll().stream().limit(8).toList());return "admin/dashboard";
    }
    @GetMapping("/products") public String listProducts(Model model){
        model.addAttribute("products",products.browse(null,null,true).stream()
                .filter(StorefrontCategoryPolicy::visibleProduct).toList());return "admin/products";
    }
    @GetMapping("/products/new") public String newProduct(Model model){
        model.addAttribute("productForm",new ProductForm());model.addAttribute("categories",StorefrontCategoryPolicy.publicCategories(categories.findAll()));
        model.addAttribute("editId",null);return "admin/product-form";
    }
    @GetMapping("/products/{id}/edit") public String editProduct(@PathVariable long id,Model model){
        model.addAttribute("productForm",products.asForm(products.get(id)));
        model.addAttribute("categories",StorefrontCategoryPolicy.publicCategories(categories.findAll()));model.addAttribute("editId",id);return "admin/product-form";
    }
    @PostMapping("/products") public String save(@Valid @ModelAttribute ProductForm productForm,BindingResult errors,
                                                  Model model,RedirectAttributes flash){
        if(errors.hasErrors()){model.addAttribute("categories",StorefrontCategoryPolicy.publicCategories(categories.findAll()));model.addAttribute("editId",null);return "admin/product-form";}
        try{products.create(productForm);flash.addFlashAttribute("success","Đã thêm sản phẩm bằng JDBC");}
        catch(RuntimeException e){model.addAttribute("error",e.getMessage());model.addAttribute("categories",StorefrontCategoryPolicy.publicCategories(categories.findAll()));return "admin/product-form";}
        return "redirect:/admin/products";
    }
    @PostMapping("/products/{id}") public String update(@PathVariable long id,
        @Valid @ModelAttribute ProductForm productForm,BindingResult errors,Model model,RedirectAttributes flash){
        if(errors.hasErrors()){model.addAttribute("categories",StorefrontCategoryPolicy.publicCategories(categories.findAll()));model.addAttribute("editId",id);return "admin/product-form";}
        try{products.edit(id,productForm);flash.addFlashAttribute("success","Đã cập nhật sản phẩm bằng JDBC");}
        catch(RuntimeException e){model.addAttribute("error",e.getMessage());model.addAttribute("categories",StorefrontCategoryPolicy.publicCategories(categories.findAll()));model.addAttribute("editId",id);return "admin/product-form";}
        return "redirect:/admin/products";
    }
    @PostMapping("/products/{id}/delete") public String delete(@PathVariable long id,RedirectAttributes flash){
        try{products.delete(id);flash.addFlashAttribute("success","Đã xóa sản phẩm");}
        catch(RuntimeException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/admin/products";
    }
    @PostMapping("/categories") public String addCategory(@RequestParam String name, RedirectAttributes flash){
        flash.addFlashAttribute("error", "Ecommerce Shop hiện chỉ có 4 danh mục: Điện thoại, Laptop, Điện máy, Phụ kiện.");
        return "redirect:/admin/products";
    }
    @GetMapping("/orders") public String orders(Model model){model.addAttribute("orders",orders.listAll());return "admin/orders";}
    @PostMapping("/orders/{id}/status") public String status(@PathVariable long id,@RequestParam String status,RedirectAttributes flash){
        try{orders.setStatus(id,status);flash.addFlashAttribute("success","Đã cập nhật trạng thái đơn hàng");}
        catch(BusinessException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/admin/orders";
    }
    @PostMapping("/qdrant/sync") public String sync(RedirectAttributes flash){
        try{int count=catalogSearch.syncProducts();flash.addFlashAttribute("success","Đã đồng bộ "+count+" sản phẩm sang Qdrant");}
        catch(RuntimeException e){flash.addFlashAttribute("error","Qdrant chưa sẵn sàng: "+e.getMessage());}
        return "redirect:/admin";
    }
}
