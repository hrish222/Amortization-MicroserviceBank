package com.example.Model;

import java.time.LocalDate;

public class PartPayment {

    private int SrNo;

    private double partpayAmount;

    private LocalDate date;
    private String type;



    public int getSrNO() {
        return SrNo;
    }

    public void setSrNO(int srNo) {
        SrNo = srNo;
    }
    public double getPartpayAmount() {
        return partpayAmount;
    }

    public void setPartpayAmount(double partpayAmount) {
        this.partpayAmount = partpayAmount;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}

