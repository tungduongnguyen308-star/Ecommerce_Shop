package com.ecom.controller;
import com.ecom.dao.CategoryDao;
import com.ecom.dao.PromotionDao;
import com.ecom.dao.ProductPromotionDao;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import com.ecom.model.Promotion;
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
    private final PromotionDao promotions;
    private final ProductPromotionDao productPromotions;
    public AdminController(ProductService products,OrderService orders,CategoryDao categories,CatalogSearchService catalogSearch,PromotionDao promotions,ProductPromotionDao productPromotions){
        this.products=products;this.orders=orders;this.categories=categories;this.catalogSearch=catalogSearch;this.promotions=promotions;this.productPromotions=productPromotions;
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
    @GetMapping("/promotions") public String promotions(Model model){model.addAttribute("promotions",promotions.findAll());return "admin/promotions";}
    @GetMapping("/promotions/new") public String newPromotion(Model model){model.addAttribute("promotion",new Promotion());model.addAttribute("editId",null);return "admin/promotion-form";}
    @GetMapping("/promotions/{id}/edit") public String editPromotion(@PathVariable long id,Model model){model.addAttribute("promotion",promotions.find(id));model.addAttribute("editId",id);return "admin/promotion-form";}
    @PostMapping("/promotions") public String createPromotion(@ModelAttribute Promotion promotion,RedirectAttributes flash){
        try{validatePromotion(promotion);promotion.setCode(promotion.getCode().trim().toUpperCase());promotions.insert(promotion);flash.addFlashAttribute("success","Đã tạo chương trình khuyến mãi");}
        catch(Exception e){flash.addFlashAttribute("error",e instanceof org.springframework.dao.DuplicateKeyException?"Mã khuyến mãi đã tồn tại":e.getMessage());return "redirect:/admin/promotions/new";}return "redirect:/admin/promotions";
    }
    @PostMapping("/promotions/{id}") public String updatePromotion(@PathVariable long id,@ModelAttribute Promotion promotion,RedirectAttributes flash){
        try{validatePromotion(promotion);promotion.setCode(promotion.getCode().trim().toUpperCase());promotions.update(id,promotion);flash.addFlashAttribute("success","Đã cập nhật khuyến mãi");}
        catch(Exception e){flash.addFlashAttribute("error",e instanceof org.springframework.dao.DuplicateKeyException?"Mã khuyến mãi đã tồn tại":e.getMessage());return "redirect:/admin/promotions/"+id+"/edit";}return "redirect:/admin/promotions";
    }
    @PostMapping("/promotions/{id}/delete") public String deletePromotion(@PathVariable long id,RedirectAttributes flash){promotions.delete(id);flash.addFlashAttribute("success","Đã xóa khuyến mãi");return "redirect:/admin/promotions";}
    @GetMapping("/product-promotions") public String productPromotions(Model model){
        model.addAttribute("productPromotions",productPromotions.findAll());
        model.addAttribute("products",products.browse(null,null,true).stream().filter(StorefrontCategoryPolicy::visibleProduct).toList());
        return "admin/product-promotions";
    }
    @PostMapping("/product-promotions") public String createProductPromotion(@RequestParam String name,@RequestParam String discountType,
            @RequestParam BigDecimal discountValue,@RequestParam LocalDate startDate,@RequestParam LocalDate endDate,
            @RequestParam(required=false) List<Long> productIds,RedirectAttributes flash){
        try{
            if(name==null||name.isBlank())throw new IllegalArgumentException("Vui lòng nhập tên chương trình");
            if(discountValue==null||discountValue.signum()<=0)throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
            if("PERCENT".equals(discountType)&&discountValue.compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException("Phần trăm giảm không được vượt quá 100%");
            if(!"PERCENT".equals(discountType)&&!"FIXED".equals(discountType))throw new IllegalArgumentException("Loại giảm không hợp lệ");
            if(startDate==null||endDate==null||endDate.isBefore(startDate))throw new IllegalArgumentException("Ngày kết thúc không hợp lệ");
            if(productIds==null||productIds.isEmpty())throw new IllegalArgumentException("Hãy chọn ít nhất một sản phẩm");
            productPromotions.create(name.trim(),discountType,discountValue,startDate,endDate,productIds);
            flash.addFlashAttribute("success","Đã tạo khuyến mãi trực tiếp cho sản phẩm");
        }catch(Exception e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/admin/product-promotions";
    }
    @PostMapping("/product-promotions/{id}/delete") public String deleteProductPromotion(@PathVariable long id,RedirectAttributes flash){
        productPromotions.delete(id);flash.addFlashAttribute("success","Đã xóa khuyến mãi sản phẩm");return "redirect:/admin/product-promotions";
    }
    private void validatePromotion(Promotion p){
        if(p.getCode()==null||!p.getCode().matches("[A-Za-z0-9_-]{3,40}"))throw new IllegalArgumentException("Mã khuyến mãi gồm 3-40 ký tự chữ, số, _ hoặc -");
        if(p.getName()==null||p.getName().isBlank())throw new IllegalArgumentException("Vui lòng nhập tên chương trình");
        if(p.getDiscountValue()==null||p.getDiscountValue().signum()<=0)throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        if("PERCENT".equals(p.getDiscountType())&&p.getDiscountValue().compareTo(new java.math.BigDecimal("100"))>0)throw new IllegalArgumentException("Mức giảm phần trăm không được vượt quá 100%");
        if(!"PERCENT".equals(p.getDiscountType())&& !"FIXED".equals(p.getDiscountType()))throw new IllegalArgumentException("Loại giảm giá không hợp lệ");
        if(p.getMinOrderValue()==null||p.getMinOrderValue().signum()<0)throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không hợp lệ");
        if(p.getStartDate()==null||p.getEndDate()==null||p.getEndDate().isBefore(p.getStartDate()))throw new IllegalArgumentException("Ngày kết thúc phải từ ngày bắt đầu trở đi");
        if(p.getUsageLimit()!=null&&p.getUsageLimit()<1)throw new IllegalArgumentException("Lượt sử dụng phải lớn hơn 0");
    }
    @PostMapping("/qdrant/sync") public String sync(RedirectAttributes flash){
        try{int count=catalogSearch.syncProducts();flash.addFlashAttribute("success","Đã đồng bộ "+count+" sản phẩm sang Qdrant");}
        catch(RuntimeException e){flash.addFlashAttribute("error","Qdrant chưa sẵn sàng: "+e.getMessage());}
        return "redirect:/admin";
    }
}
