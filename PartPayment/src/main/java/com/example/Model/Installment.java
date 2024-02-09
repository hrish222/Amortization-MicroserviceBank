package com.example.Model;

import java.time.LocalDate;

public class Installment {
    private int installmentNumber;
    private String emiDate;
    private String openingPrincipal;
    private String closingPrincipal;
    private String principalAmount;
    private String interestAmount;
    private String emi;
    private String totalInterestPaid;

    public Installment() {
    }

    public Installment(int installmentNumber, String emiDate, String openingPrincipal, String closingPrincipal, String principalAmount, String interestAmount, String emi, String totalInterestPaid) {
        this.installmentNumber = installmentNumber;
        this.emiDate = emiDate;
        this.openingPrincipal = openingPrincipal;
        this.closingPrincipal = closingPrincipal;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.emi = emi;
        this.totalInterestPaid = totalInterestPaid;
    }


    public String getEmiDate() {
        return emiDate;
    }

    public void setEmiDate(String emiDate) {
        this.emiDate = emiDate;
    }

    public void setEmiDate() {
        this.emiDate = emiDate;
    }

    public String getOpeningPrincipal() {
        return openingPrincipal;
    }

    public void setOpeningPrincipal(String openingPrincipal) {
        this.openingPrincipal = openingPrincipal;
    }

    public String getClosingPrincipal() {
        return closingPrincipal;
    }

    public void setClosingPrincipal(String closingPrincipal) {
        this.closingPrincipal = closingPrincipal;
    }

    public String getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(String principalAmount) {
        this.principalAmount = principalAmount;
    }

    public String getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(String interestAmount) {
        this.interestAmount = interestAmount;
    }

    public String getEmi() {
        return emi;
    }

    public void setEmi(String emi) {
        this.emi = emi;
    }

    public String getTotalInterestPaid() {
        return totalInterestPaid;
    }

    public void setTotalInterestPaid(String totalInterestPaid) {
        this.totalInterestPaid = totalInterestPaid;
    }


    public int getInstallmentNumber() {
        return installmentNumber;
    }
    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }
}
