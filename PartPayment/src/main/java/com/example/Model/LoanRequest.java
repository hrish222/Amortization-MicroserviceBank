package com.example.Model;

import java.util.Date;
import java.util.List;

public class LoanRequest {
    private double loanAmount;
    private double interestRate;
    private int tenure;
    private Date disbursalDate;
    private int emiDay;
    private List<PartPayment> partPayments;

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getTenure() {
        return tenure;
    }

    public void setTenure(int tenure) {
        this.tenure = tenure;
    }
    public Date getDisbursalDate() {
        return disbursalDate;
    }

    public void setDisbursalDate(Date disbursalDate) {
        this.disbursalDate = disbursalDate;
    }

    public int getEmiDay() {
        return emiDay;
    }


    public void setEmiDay(int emiDay) {
        if (emiDay >= 1 && emiDay <= 31) {
            this.emiDay = emiDay;
        } else {
            throw new InvalidEmiDayException("Invalid emiDay value. It should be between 1 and 31 (inclusive).");
        }
    }

    public List<PartPayment> getPartPayments() {
        return partPayments;
    }

    public void setPartPayments(List<PartPayment> partPayments) {
        this.partPayments = partPayments;
    }

}
