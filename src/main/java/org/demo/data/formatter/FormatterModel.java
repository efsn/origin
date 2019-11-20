package org.demo.data.formatter;

import org.demo.data.PhoneNumber;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import java.util.Date;

public class FormatterModel {

    @NumberFormat(style = Style.NUMBER, pattern = "#,###")
    private int totalCount;

    @NumberFormat(style = Style.PERCENT)
    private double discount;

    @NumberFormat(style = Style.CURRENCY)
    private double sumMoney;

    @DateTimeFormat(pattern = "yyyy-MM-ddHH:mm:ss")
    private Date date;

    @PhoneNumberA
    private PhoneNumber phoneNumber;

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(org.demo.data.PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getSumMoney() {
        return sumMoney;
    }

    public void setSumMoney(double sumMoney) {
        this.sumMoney = sumMoney;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date rdate) {
        this.date = rdate;
    }

}
