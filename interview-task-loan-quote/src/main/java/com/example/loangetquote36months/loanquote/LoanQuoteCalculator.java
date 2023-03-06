package com.example.loangetquote36months.loanquote;

import com.example.loangetquote36months.marketdata.MarketData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoanQuoteCalculator {
    private final List<MarketData> marketDataList;

    public LoanQuoteCalculator(List<MarketData> marketDataList) {
        Objects.requireNonNull(marketDataList, "Market data List cannot be null");
        this.marketDataList = marketDataList;
    }

    public Optional<LoanQuote> getQuote() {
        return marketDataList.stream()
                .map(this::getQuoteForLender)
                .reduce((left, right) -> {
                    // TODO this could be separate method - easier for testing
                    var loanAmount = left.getLoanAmount()
                            .add(right.getLoanAmount());
                    var averageRate = left.getAverageRate()
                            .add(right.getAverageRate())
                            .setScale(14, RoundingMode.UP)
                            .divide(BigDecimal.valueOf(2), RoundingMode.UP);
                    var monthlyRepayment = left.getMonthlyRepayment()
                            .add(right.getMonthlyRepayment());
                    var totalRepayment = left.getTotalRepayment()
                            .add(right.getTotalRepayment());
                    return new LoanQuote(loanAmount, averageRate, monthlyRepayment, totalRepayment);
                });
    }

    // @VisibleForTesting
    LoanQuote getQuoteForLender(MarketData marketDataLender) {
        return new LoanQuotePerLenderCalculator(marketDataLender)
                .getLoanQuote();
    }
}
