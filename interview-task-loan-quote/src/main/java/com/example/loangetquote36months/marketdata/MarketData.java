package com.example.loangetquote36months.marketdata;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class MarketData {
    private String lender;
    private BigDecimal rate;
    private BigInteger amountToLend;
}
