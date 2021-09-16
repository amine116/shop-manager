package com.amine.bmanager;

public class SoldProduct extends CartProduct{
    private String payableAmount, paid, due, sellingDate, buyerName, phone;

    public SoldProduct(){}

    public SoldProduct(String productCode, String productName, String reservedAmount,
                       String purchaseDate, String purchasePricePerUnit, String sellingPricePerUnit,
                       String companyName, String sellAmount, String payableAmount, String paid,
                       String due, String sellingDate, String buyerName, String phone) {

        super(productCode, productName, reservedAmount, purchaseDate, purchasePricePerUnit,
                sellingPricePerUnit, companyName, sellAmount);

        this.payableAmount = payableAmount;
        this.paid = paid;
        this.due = due;
        this.sellingDate = sellingDate;
        this.buyerName = buyerName;
        this.phone = phone;
    }

    public String getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(String payableAmount) {
        this.payableAmount = payableAmount;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public String getSellingDate() {
        return sellingDate;
    }

    public void setSellingDate(String sellingDate) {
        this.sellingDate = sellingDate;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
