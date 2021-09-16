package com.amine.bmanager;

public class Product {

    private String productCode, productName, reservedAmount, purchaseDate,
            purchasePricePerUnit, sellingPricePerUnit, companyName;

    public Product() { }

    public Product(String productCode, String productName, String reservedAmount,
                   String purchaseDate, String purchasePricePerUnit, String sellingPricePerUnit, String companyName) {
        this.productCode = productCode;
        this.productName = productName;
        this.reservedAmount = reservedAmount;
        this.purchaseDate = purchaseDate;
        this.purchasePricePerUnit = purchasePricePerUnit;
        this.sellingPricePerUnit = sellingPricePerUnit;
        this.companyName = companyName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(String reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchasePricePerUnit() {
        return purchasePricePerUnit;
    }

    public void setPurchasePricePerUnit(String purchasePricePerUnit) {
        this.purchasePricePerUnit = purchasePricePerUnit;
    }

    public String getSellingPricePerUnit() {
        return sellingPricePerUnit;
    }

    public void setSellingPricePerUnit(String sellingPricePerUnit) {
        this.sellingPricePerUnit = sellingPricePerUnit;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
