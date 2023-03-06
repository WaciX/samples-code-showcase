package com.example.loangetquote36months.loanquote;

import com.example.loangetquote36months.marketdata.MarketData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoanQuoteCalculatorTest {

    @Test
    void constructor_nullMarketData_throwsNPEWithMessage() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new LoanQuoteCalculator(null), "Market data List cannot be null");
    }

    @Test
    void getQuote_emptyMarketData_notNull() {
        var calculator = new LoanQuoteCalculator(List.of());

        var quote = calculator.getQuote();

        assertThat(quote, notNullValue());
        assertThat(quote.isEmpty(), is(true));
    }

    @Test
    void getQuote_twoMarketDatas_twoQuotes() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(500));
        var marketData2 = new MarketData("lender2", BigDecimal.valueOf(0.2), BigInteger.valueOf(1000));
        var quote1 = new LoanQuote(BigInteger.valueOf(500), BigDecimal.valueOf(0.1), BigDecimal.valueOf(12.34),
                BigDecimal.valueOf(678.90));
        var calculator = spy(new LoanQuoteCalculator(List.of(marketData1, marketData2)));
        doReturn(quote1).when(calculator).getQuoteForLender(any());

        calculator.getQuote();

        verify(calculator, times(2)).getQuoteForLender(any());
        verify(calculator).getQuoteForLender(eq(marketData1));
        verify(calculator).getQuoteForLender(eq(marketData2));
    }

    @Test
    void getQuote_twoMarketDatas_reducedToOne() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(500));
        var marketData2 = new MarketData("lender2", BigDecimal.valueOf(0.2), BigInteger.valueOf(1000));
        var quote1 = new LoanQuote(BigInteger.valueOf(500), BigDecimal.valueOf(0.1), BigDecimal.valueOf(12.34),
                BigDecimal.valueOf(678.90));
        var quote2 = new LoanQuote(BigInteger.valueOf(1000), BigDecimal.valueOf(0.2), BigDecimal.valueOf(23.45),
                BigDecimal.valueOf(1234.56));
        var calculator = spy(new LoanQuoteCalculator(List.of(marketData1, marketData2)));
        doReturn(quote1).when(calculator).getQuoteForLender(eq(marketData1));
        doReturn(quote2).when(calculator).getQuoteForLender(eq(marketData2));

        var quote = calculator.getQuote();

        assertThat(quote.isPresent(), is(true));
        var loanQuote = quote.get();
        assertThat(loanQuote.getLoanAmount(), is(BigInteger.valueOf(1500)));
        assertThat(loanQuote.getAverageRate(), closeTo(BigDecimal.valueOf(0.15), BigDecimal.valueOf(0.000001)));
        assertThat(loanQuote.getMonthlyRepayment(), closeTo(BigDecimal.valueOf(35.79), BigDecimal.valueOf(0.000001)));
        assertThat(loanQuote.getTotalRepayment(), closeTo(BigDecimal.valueOf(1913.46), BigDecimal.valueOf(0.000001)));
    }
}
