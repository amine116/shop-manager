package com.amine.bmanager;

public class CartProduct extends Product {
    private String sellAmount;

    public CartProduct(){}

    public CartProduct(String productCode, String productName, String reservedAmount,
                       String purchaseDate, String purchasePricePerUnit, String sellingPricePerUnit,
                       String companyName, String sellAmount) {

        super(productCode, productName, reservedAmount, purchaseDate,
                purchasePricePerUnit, sellingPricePerUnit, companyName);
        this.sellAmount = sellAmount;

    }

    public String getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(String sellAmount) {
        this.sellAmount = sellAmount;
    }
}
