package com.example.loangetquote36months.loanquote;

import com.example.loangetquote36months.marketdata.MarketData;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Picks the best lenders deals that will satisfy the requested loan amount.
 * The last market loan amount might be lower than the amount available in the del - sum of all deals amounts needs to
 * be equal to requested loan amount.
 */
public class BestLendersPicker {

    private static final BigInteger BIGINT_100 = BigInteger.valueOf(100);

    private final List<MarketData> marketDataList;
    private final BigInteger loanAmount;

    public BestLendersPicker(List<MarketData> marketDataList, BigInteger loanAmount) {
        Objects.requireNonNull(marketDataList, "Market data List cannot be null");
        Objects.requireNonNull(loanAmount, "Loan amount cannot be null");

        this.marketDataList = marketDataList;
        this.loanAmount = loanAmount;
    }

    public List<MarketData> getBestDeals() {
        var amountToBeProvided = new AtomicReference<>(roundToNearest100Up(loanAmount));

        // TODO this is broken - see tests
        // TODO the last returned MarketData amount should reduce the loanAmount to the leftNumber
        var deals = marketDataList.stream()
                .sorted(Comparator.comparing(MarketData::getRate)
                        .thenComparing(Comparator.comparing(MarketData::getAmountToLend).reversed()))
                .takeWhile(marketData -> {
                    // Don't look for any more deals if we already found the best ones
                    if (amountToBeProvided.get().signum() <= 0) {
                        return false;
                    }

                    var amountLeft = amountToBeProvided.get()
                            .subtract(marketData.getAmountToLend());
                    amountToBeProvided.set(amountLeft);

                    return true;
                })
                .collect(Collectors.toList());

        // No deals sufficient to satisfy requested loan amount
        if (amountToBeProvided.get().signum() > 0) {
            return List.of();
        }

        // Reduce the amount for last deal
        if (!deals.isEmpty()) {
            var lastMarketData = deals.get(deals.size() - 1);
            // Add, as the amount left is negative
            var newLastMarketData= lastMarketData.toBuilder()
                    .amountToLend(lastMarketData.getAmountToLend().add(amountToBeProvided.get()))
                    .build();
            deals.set(deals.size() - 1, newLastMarketData);
        }
        return deals;
    }

    // TODO this is probably nto the best place
    // @VisibleForTesting
    BigInteger roundToNearest100Up(BigInteger value) {
        var remainder = value.mod(BIGINT_100);
        if (remainder.equals(BigInteger.ZERO)) {
            // It's already nice and round.
            return value;
        }
        return value.subtract(remainder).add(BIGINT_100);
    }
}
