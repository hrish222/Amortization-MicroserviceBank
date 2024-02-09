package com.example.Controller;

import com.example.Model.Installment;
import com.example.Model.LoanRequest;
import com.example.Model.PartPayment;
;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Calendar.MONTH;


@RestController
public class PartpayController {

    private DecimalFormat df = new DecimalFormat("#.##");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        @PostMapping("/emi-cal")
            ResponseEntity<Map<String, Object>> calculateEMIDetails(@RequestBody LoanRequest loanDetails) {
            List<List<Installment>> installmentSegments = new ArrayList<>(); // List to store segments
            List<Installment> currentSegment = new ArrayList<>(); // Current segment

            double loanAmount = loanDetails.getLoanAmount();
            double interestRate = loanDetails.getInterestRate();
            int tenure = loanDetails.getTenure();
            Date disbursalDate = loanDetails.getDisbursalDate();
            int randomDay = loanDetails.getEmiDay();

            LocalDate localDisbursalDate = disbursalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate localRandomDate = LocalDate.of(localDisbursalDate.getYear(), localDisbursalDate.getMonth(), randomDay);

            if (localRandomDate.isBefore(localDisbursalDate)) {
                localRandomDate = localRandomDate.plusMonths(1);
            }

            LocalDate localactualEmiDate = localRandomDate.plusMonths(1);
            Date actualEmiDate = Date.from(localRandomDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate localFirstEmiDate = localactualEmiDate;

            Date firstEmiDate = Date.from(localFirstEmiDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(actualEmiDate);

            calendar.add(MONTH, 1);
            double monthlyInterestRate = interestRate / (12 * 100);
            double emi = loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, tenure)
                    / (Math.pow(1 + monthlyInterestRate, tenure) - 1);

            double irr = calculateIRR(loanAmount, emi, monthlyInterestRate, tenure);
            double irrPercentage = irr * 100.0;
            long daysDiff = ChronoUnit.DAYS.between(localDisbursalDate, localRandomDate);

            double calbpi = loanAmount * interestRate * daysDiff / 36000.0;
            double bpi = Math.ceil(calbpi * 100) / 100.0;

            double openingPrincipal = loanAmount;
            int i;
            LocalDate partpayDate = null;

            List<PartPayment> sortedPartPayments = loanDetails.getPartPayments()
                    .stream()
                    .sorted(Comparator.comparing(PartPayment::getDate))
                    .collect(Collectors.toList());

            List<LocalDate> installmentDates = calculateInstallmentDates(localFirstEmiDate, tenure, sortedPartPayments);
            int partPaymentIndex = 0;

            double totalInterestPaid = 0;
            boolean partPaymentOccurred = false;
            boolean reduceEmiOccurred = false;
            boolean reduceTenureOccurred = false;
            int tene1 = 0;
            List<Installment> after = new ArrayList<>();
            double closingPrincipalr = 0;
            double closingPrincipalr1 = 0;
            double totinterest = 0;
            double totlinterest = 0;
            boolean canReduceTenure=true;
            String latestPartPaymentType = "";

            Object partpayments = loanDetails.getPartPayments();

            if (partpayments != null && !loanDetails.getPartPayments().isEmpty()) {

            } else {
                System.out.println("Part payments are empty. Adding currentSegment.");
                installmentSegments.add(currentSegment);
            }

            for (i = 1; i <= tenure; i++) {
                LocalDate currentDate = installmentDates.get(i - 1);

                boolean partPaymentProcessed = false;
                double interestAmount = openingPrincipal * monthlyInterestRate;
                double principalAmount = emi - interestAmount;
                double closingPrincipal = openingPrincipal - principalAmount;


                for (PartPayment partPayment : loanDetails.getPartPayments()) {
                    if (currentDate.isAfter(localFirstEmiDate) || currentDate.isEqual(localFirstEmiDate)) {
                        while (partPaymentIndex < sortedPartPayments.size()) {
                            PartPayment currentPartPayment = sortedPartPayments.get(partPaymentIndex);

                            if (currentPartPayment.getDate().isBefore(currentDate) ||
                                    currentPartPayment.getDate().isEqual(currentDate)) {
                                double partPaymentAmount = currentPartPayment.getPartpayAmount();

                                openingPrincipal -= partPaymentAmount;
                                interestAmount = openingPrincipal * monthlyInterestRate;

                                partPaymentProcessed = true;
                                partPaymentIndex++;


                                if ("REDUCE_TENURE".equals(currentPartPayment.getType())) {

                                    reduceTenureOccurred = true;

                                    loanAmount = openingPrincipal;
                                    int reducedTenure = calculateReducedTenure(partPaymentAmount, loanAmount, emi, monthlyInterestRate);
//                                    int reducedTenure = calculateTenure( loanAmount, emi, monthlyInterestRate);
                                    tenure -= reducedTenure;
                                    tene1 = tenure;
//                                    loanAmount -= partPaymentAmount;
                                    emi = calculateEmi(loanAmount, monthlyInterestRate, tenure);
                                    principalAmount = emi - interestAmount;
                                    closingPrincipal = openingPrincipal - principalAmount;
                                    partpayDate = currentDate;
                                    closingPrincipalr1 = openingPrincipal;
                                    totinterest = totalInterestPaid;
                                    canReduceTenure=true;
                                    latestPartPaymentType = "REDUCE_TENURE";



                                } else if ("REDUCE_EMI".equals(currentPartPayment.getType())) {

                                    reduceEmiOccurred = true;
                                    loanAmount = openingPrincipal;
                                    emi = calculatereduceemi(loanAmount, monthlyInterestRate, tenure);
                                    principalAmount = emi - interestAmount;
                                    closingPrincipal = openingPrincipal - principalAmount;
                                    closingPrincipalr = openingPrincipal;
                                    totlinterest = totalInterestPaid;

                                    partpayDate = currentDate;
                                    latestPartPaymentType = "REDUCE_EMI";

                                }

                            } else {
                                break;
                            }

                        }

                    }
                }

                if (reduceEmiOccurred) {
                    principalAmount = Math.min(openingPrincipal, emi - interestAmount);
                    closingPrincipal = openingPrincipal - principalAmount;


                }
                else if (reduceTenureOccurred) {
                    principalAmount = Math.min(openingPrincipal, emi - interestAmount);
                    closingPrincipal = openingPrincipal - principalAmount;

                }
                else {
                    principalAmount = emi - interestAmount;
                    closingPrincipal = openingPrincipal - principalAmount;

                }


                totalInterestPaid += interestAmount;

                Installment installment = new Installment();
                installment.setInstallmentNumber(i);
                installment.setEmiDate(sdf.format(calendar.getTime()));
                installment.setOpeningPrincipal(df.format(openingPrincipal));
                installment.setClosingPrincipal(df.format(closingPrincipal));
                installment.setPrincipalAmount(df.format(principalAmount));
                installment.setInterestAmount(df.format(interestAmount));
                installment.setEmi(df.format(emi));
                installment.setTotalInterestPaid(df.format(totalInterestPaid));

                if (partPaymentProcessed) {
                    installmentSegments.add(currentSegment);
                    currentSegment = new ArrayList<>();
                    reduceTenureOccurred = false;
                    reduceEmiOccurred = false;
                }

                currentSegment.add(installment);

                openingPrincipal = closingPrincipal;
                calendar.add(MONTH, 1);


            }
            if ((closingPrincipalr >= 0 && canReduceTenure) || (closingPrincipalr1 >= 0 && canReduceTenure)) {
                if ("REDUCE_TENURE".equals(latestPartPaymentType)) {
                    Date partdate = Date.from(partpayDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                calendar.setTime(partdate);
                    double openingPrincipal1 = closingPrincipalr1;
                    int tenure1 = tene1;
                    double totalInterestPaid1 = totinterest;

                    for (int j = 1; j <= tenure1; j++) {
                        if (!(openingPrincipal1 == 0)) {
                        double interestAmount = openingPrincipal1 * monthlyInterestRate;
                        double principalAmount = emi - interestAmount;
                        double closingPrincipal = openingPrincipal1 - principalAmount;
                        totalInterestPaid1 += interestAmount;

                        Installment sd = new Installment();
                        sd.setInstallmentNumber(j);
                        sd.setEmiDate(sdf.format(calendar.getTime()));
                        sd.setOpeningPrincipal(df.format(openingPrincipal1));
                        sd.setClosingPrincipal(df.format(closingPrincipal));
                        sd.setPrincipalAmount(df.format(principalAmount));
                        sd.setInterestAmount(df.format(interestAmount));
                        sd.setEmi(df.format(emi));
                        sd.setTotalInterestPaid(df.format(totalInterestPaid1));

                        after.add(sd);
                        openingPrincipal1 = closingPrincipal;

                        calendar.add(Calendar.MONTH, 1);
                    }}
                } else if ("REDUCE_EMI".equals(latestPartPaymentType)) {
                    Date partdate = Date.from(partpayDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                calendar.setTime(partdate);
                    double openingPrincipalReducedEmi = closingPrincipalr;
                    double totalInterestPaidReducedEmi = totlinterest;

                    for (int k = 1; k <= tenure; k++) {
                        if (!(openingPrincipalReducedEmi == 0)) {
                            double interestAmount = openingPrincipalReducedEmi * monthlyInterestRate;
                            double principalAmount = emi - interestAmount;
                            double closingPrincipal = openingPrincipalReducedEmi - principalAmount;
                            totalInterestPaidReducedEmi += interestAmount;

                            Installment sd = new Installment();
                            sd.setInstallmentNumber(k);
                            sd.setEmiDate(sdf.format(calendar.getTime()));
                            sd.setOpeningPrincipal(df.format(openingPrincipalReducedEmi));
                            sd.setClosingPrincipal(df.format(closingPrincipal));
                            sd.setPrincipalAmount(df.format(principalAmount));
                            sd.setInterestAmount(df.format(interestAmount));
                            sd.setEmi(df.format(emi));
                            sd.setTotalInterestPaid(df.format(totalInterestPaidReducedEmi));

                            after.add(sd);
                            openingPrincipalReducedEmi = closingPrincipal;

                            calendar.add(Calendar.MONTH, 1);
                        }
                    }
                }
            }
             installmentSegments.add(after);





            List<Map<Integer, List<Installment>>> segmentResponses = generateSegmentResponses(installmentSegments);


                Map<String, Object> response = new LinkedHashMap<>();
                response.put("Amortization-Table", segmentResponses);
                response.put("irr", df.format(irrPercentage));
                response.put("BPI", bpi);
                response.put("actualEmiDate", sdf.format(Date.from(actualEmiDate.toInstant())));
                response.put("firstEmiDate", sdf.format(Date.from(firstEmiDate.toInstant())));
                response.put("daysDiff", daysDiff);

                return ResponseEntity.ok(response);
            }




    public static List<Map<Integer, List<Installment>>> generateSegmentResponses(List < List < Installment >> installmentSegments) {
                List<Map<Integer, List<Installment>>> segmentResponses = new ArrayList<>();

                int segmentNumber = 1;
                for (List<Installment> segment : installmentSegments) {
                    Map<Integer, List<Installment>> segmentResponse = new LinkedHashMap<>();
                    segmentResponse.put(segmentNumber, segment);
//            segmentResponse.put("", segment);
                    segmentResponses.add(segmentResponse);
                    segmentNumber++;
                }

                return segmentResponses;
            }


            private static double calculateIRR ( double loanAmount, double emi, double monthlyInterestRate, int tenure){
                double epsilon = 1e-7;
                double guess = 0.1;

                while (true) {
                    double npv = -loanAmount;
                    double npvDerivative = 0.0;

                    for (int i = 1; i <= tenure; i++) {
                        double discountFactor = Math.pow(1.0 + guess, i);
                        npv += emi / discountFactor;
                        npvDerivative -= i * emi / (discountFactor * (1.0 + guess));
                    }

                    if (Math.abs(npv) < epsilon) {
                        break;
                    }

                    guess -= npv / npvDerivative;
                }

                return guess;
            }

            private List<LocalDate> calculateInstallmentDates (LocalDate start,int tenure, List<
            PartPayment > partPayments){
                List<LocalDate> installmentDates = new ArrayList<>();

                LocalDate currentDate = start;
                for (int i = 0; i <= tenure; i++) {
                    LocalDate finalCurrentDate = currentDate;
                    boolean isPartPaymentDate = partPayments.stream().anyMatch(pp -> pp.getDate().equals(finalCurrentDate));
                    if (!isPartPaymentDate) {
                        installmentDates.add(currentDate);
                    }
                    currentDate = currentDate.plusMonths(1);
                }

                return installmentDates;
            }

            private double calculateEmi ( double loanAmount, double monthlyInterestRate, int tenure){
                return loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, tenure)
                        / (Math.pow(1 + monthlyInterestRate, tenure) - 1);
            }

            private int calculateReducedTenure ( double partPaymentAmount, double loanAmount, double emi, double monthlyInterestRate){
                double remainingLoanAmount = loanAmount - partPaymentAmount;
                int reducedTenure = (int) Math.ceil(Math.log(emi / (emi - remainingLoanAmount * monthlyInterestRate)) / Math.log(1 + monthlyInterestRate));
                return reducedTenure;
            }
            private double calculatereduceemi ( double loanAmount, double monthlyInterestRate, int tenure){

                return loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, tenure)
                        / (Math.pow(1 + monthlyInterestRate, tenure) - 1);
            }

//    private int calculateTenure ( double loanAmount, double emi, double monthlyInterestRate){
//                // Calculate tenure based on reduced EMI
//                // Formula: Tenure = (log(emi) - log(emi - r * p)) / log(1 + r), where r = monthlyInterestRate
//                double r = monthlyInterestRate;
//                double p = loanAmount;
//                double n = (Math.log(emi) - Math.log(emi - r * p)) / Math.log(1 + r);
//                return (int) Math.ceil(n); // Round up to ensure it's a whole number of months
//            }


}
















