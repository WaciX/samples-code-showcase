package com.example.loangetquote36months.loanquote;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class LoanQuote {
    private BigInteger loanAmount;
    private BigDecimal averageRate;
    private BigDecimal monthlyRepayment;
    private BigDecimal totalRepayment;
}
