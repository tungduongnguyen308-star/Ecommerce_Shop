package com.ecom.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Promotion {
    private Long id;
    private String code;
    private String name;
    private String discountType = "PERCENT";
    private BigDecimal discountValue = BigDecimal.ZERO;
    private BigDecimal minOrderValue = BigDecimal.ZERO;
    private BigDecimal maxDiscount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer usageLimit;
    private Integer usedCount = 0;
    private boolean active = true;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getCode(){return code;} public void setCode(String v){code=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDiscountType(){return discountType;} public void setDiscountType(String v){discountType=v;}
    public BigDecimal getDiscountValue(){return discountValue;} public void setDiscountValue(BigDecimal v){discountValue=v;}
    public BigDecimal getMinOrderValue(){return minOrderValue;} public void setMinOrderValue(BigDecimal v){minOrderValue=v;}
    public BigDecimal getMaxDiscount(){return maxDiscount;} public void setMaxDiscount(BigDecimal v){maxDiscount=v;}
    public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
    public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
    public Integer getUsageLimit(){return usageLimit;} public void setUsageLimit(Integer v){usageLimit=v;}
    public Integer getUsedCount(){return usedCount;} public void setUsedCount(Integer v){usedCount=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
