package com.example.loangetquote36months.loanquote;

import com.example.loangetquote36months.marketdata.MarketData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class BestLendersPickerTest {

    @Test
    void constructor_nullMarketDataList_throwsNPEWithMessage() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new BestLendersPicker(null, BigInteger.ONE), "Market data List cannot be null");
    }

    @Test
    void constructor_nullAmount_throwsNPEWithMessage() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new BestLendersPicker(List.of(), null), "Loan amount cannot be null");
    }

    @Test
    void getBestDeals_marketDataListEmpty_emptyList() {
        var picker = new BestLendersPicker(List.of(), BigInteger.ONE);

        var list = picker.getBestDeals();

        assertThat(list, hasSize(0));
    }

    @Test
    void getBestDeals_amountNotRounded_roundedUpCalled() {
        var picker = Mockito.spy(new BestLendersPicker(List.of(), BigInteger.TWO));
        Mockito.doReturn(BigInteger.TEN).when(picker).roundToNearest100Up(Mockito.any());

        picker.getBestDeals();

        Mockito.verify(picker).roundToNearest100Up(Mockito.eq(BigInteger.TWO));
    }

    @Test
    void getBestDeals_loanAmountNotSufficientForAnyDeal_emptyList() {
        var marketData1 = new MarketData("lender1", BigDecimal.ONE, BigInteger.valueOf(499));
        var marketData2 = new MarketData("lender2", BigDecimal.ONE, BigInteger.valueOf(500));
        var picker = new BestLendersPicker(List.of(marketData1, marketData2), BigInteger.valueOf(1000));

        var list = picker.getBestDeals();

        assertThat(list, hasSize(0));
    }

    @Test
    void getBestDeals_loanAmountEqualForADeal_twoMarketDatas() {
        var marketData1 = new MarketData("lender1", BigDecimal.ONE, BigInteger.valueOf(500));
        var marketData2 = new MarketData("lender2", BigDecimal.ONE, BigInteger.valueOf(500));
        var picker = new BestLendersPicker(List.of(marketData1, marketData2), BigInteger.valueOf(1000));

        var list = picker.getBestDeals();

        assertThat(list, is(List.of(marketData1, marketData2)));
    }

    @Test
    void getBestDeals_dealAvailableAmountsHigherThanRequestedLoanAmount_lastMarketDataAmountReducedToBeEqualToRequestedLoanAmount() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(500));
        var marketData2 = new MarketData("lender2", BigDecimal.valueOf(0.2), BigInteger.valueOf(1000));
        var picker = new BestLendersPicker(List.of(marketData1, marketData2), BigInteger.valueOf(1000));

        var list = picker.getBestDeals();

        assertThat(list, is(List.of(marketData1,
                marketData2.toBuilder().amountToLend(BigInteger.valueOf(500)).build())));
    }

    @Test
    void getBestDeals_pickedBetterDeal_onlyOneMarketData() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(1000));
        var marketData2 = new MarketData("lender2", BigDecimal.valueOf(0.2), BigInteger.valueOf(1000));
        var picker = new BestLendersPicker(List.of(marketData1, marketData2), BigInteger.valueOf(1000));

        var list = picker.getBestDeals();

        assertThat(list, is(List.of(marketData1)));
    }

    @Test
    void roundToNearest100Up_numberDoesNotNeedRounding_theSameNumber() {
        var picker = new BestLendersPicker(List.of(), BigInteger.ONE);

        var rounded = picker.roundToNearest100Up(BigInteger.valueOf(1300));

        assertThat(rounded, is(BigInteger.valueOf(1300)));
    }

    @Test
    void roundToNearest100Up_numberNotRounded_roundedToNearest100Up() {
        var picker = new BestLendersPicker(List.of(), BigInteger.ONE);

        var rounded = picker.roundToNearest100Up(BigInteger.valueOf(1234));

        assertThat(rounded, is(BigInteger.valueOf(1300)));
    }
}
