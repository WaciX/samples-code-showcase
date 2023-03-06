package com.example.loangetquote36months.loanquote;

import com.example.loangetquote36months.marketdata.MarketData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

public class LoanQuotePerLenderCalculator {

    // TODO this should be configurable in constructor
    private static final int REPAYMENT_MONTHS = 36;

    private final MarketData marketData;

    public LoanQuotePerLenderCalculator(MarketData marketData) {
        Objects.requireNonNull(marketData, "Market data cannot be null");
        this.marketData = marketData;
    }

    public LoanQuote getLoanQuote() {
        var monthlyRate = marketData.getRate().setScale(14, RoundingMode.UP)
                .divide(BigDecimal.valueOf(12), RoundingMode.UP);

        var monthlyRepayment = getMonthlyRepayment(monthlyRate, marketData.getAmountToLend(), REPAYMENT_MONTHS);

        var totalRepayment = monthlyRepayment.multiply(BigDecimal.valueOf(REPAYMENT_MONTHS));

        return new LoanQuote(marketData.getAmountToLend(), marketData.getRate(), monthlyRepayment, totalRepayment);
    }

    // Formula from https://en.wikipedia.org/wiki/Mortgage_calculator
    // @VisibleForTesting
    BigDecimal getMonthlyRepayment(BigDecimal monthlyRate, BigInteger amountBorrowed, int monthsPayments) {
        if (monthlyRate.equals(BigDecimal.ZERO)) {
            return new BigDecimal(amountBorrowed)
                    .setScale(14, RoundingMode.UP)
                    .divide(BigDecimal.valueOf(monthsPayments), RoundingMode.UP);
        }

        var onePlusRatePowNoRates = BigDecimal.ONE.add(monthlyRate)
                .setScale(14, RoundingMode.UP)
                .pow(monthsPayments);
        return monthlyRate.setScale(14, RoundingMode.UP)
                .multiply(new BigDecimal(amountBorrowed))
                .multiply(onePlusRatePowNoRates)
                .divide(onePlusRatePowNoRates.subtract(BigDecimal.ONE), RoundingMode.UP);
    }
}
