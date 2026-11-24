package com.ecom.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class ProductForm {
    @NotBlank(message="Tên sản phẩm không được bỏ trống") @Size(max=200)
    private String name;
    @NotBlank(message="Mô tả không được bỏ trống") @Size(max=3000)
    private String description;
    @NotNull(message="Chọn danh mục") @Min(1)
    private Long categoryId;
    @NotNull(message="Nhập giá") @DecimalMin(value="0.01", message="Giá phải lớn hơn 0")
    private BigDecimal price;
    @NotNull @Min(value=0, message="Tồn kho phải từ 0 trở lên")
    private Integer stock;
    @NotBlank(message="Nhập URL ảnh") @Size(max=400)
    private String imageUrl="/img/ecom.png";
    private boolean active=true;
    public String getName(){return name;} public void setName(String s){name=s;}
    public String getDescription(){return description;} public void setDescription(String s){description=s;}
    public Long getCategoryId(){return categoryId;} public void setCategoryId(Long x){categoryId=x;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal x){price=x;}
    public Integer getStock(){return stock;} public void setStock(Integer x){stock=x;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String s){imageUrl=s;}
    public boolean isActive(){return active;} public void setActive(boolean x){active=x;}
}
