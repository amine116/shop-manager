package com.amine.bmanager;

public class CategorizedSearched {
    String categoryName, totalPayableAmount, paid, due, phone;

    public CategorizedSearched(){}

    public CategorizedSearched(String categoryName, String totalPayableAmount, String paid,
                               String due, String phone) {
        this.categoryName = categoryName;
        this.totalPayableAmount = totalPayableAmount;
        this.paid = paid;
        this.due = due;
        this.phone = phone;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTotalPayableAmount() {
        return totalPayableAmount;
    }

    public void setTotalPayableAmount(String totalPayableAmount) {
        this.totalPayableAmount = totalPayableAmount;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
