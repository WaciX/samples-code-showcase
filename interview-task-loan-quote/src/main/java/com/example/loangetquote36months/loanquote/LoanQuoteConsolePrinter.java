package com.example.loangetquote36months.loanquote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class LoanQuoteConsolePrinter {
    private final LoanQuote loanQuote;

    public LoanQuoteConsolePrinter(LoanQuote loanQuote) {
        Objects.requireNonNull(loanQuote, "Loan quote cannot be null");
        this.loanQuote = loanQuote;
    }

    public void print() {
        String sb = "\nRequested amount: £" + loanQuote.getLoanAmount()
                + "\nRate: " + loanQuote.getAverageRate().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.UP) + "%"
                + "\nMonthly repayment: £" + loanQuote.getMonthlyRepayment().setScale(2, RoundingMode.UP)
                + "\nTotal repayment: £" + loanQuote.getTotalRepayment().setScale(2, RoundingMode.UP);
        System.out.println(sb);
    }
}
