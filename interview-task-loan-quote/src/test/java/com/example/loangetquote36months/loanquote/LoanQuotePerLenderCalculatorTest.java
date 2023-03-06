package com.example.loangetquote36months.loanquote;

import com.example.loangetquote36months.marketdata.MarketData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoanQuotePerLenderCalculatorTest {

    @Test
    void constructor_nullMarketData_throwsNPEWithMessage() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new LoanQuotePerLenderCalculator(null), "Market data cannot be null");
    }

    @Test
    void getLoanQuote_haveMarketData_neverReturnsNull() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(500));
        var calculator = new LoanQuotePerLenderCalculator(marketData1);

        var quote = calculator.getLoanQuote();

        assertThat(quote, notNullValue());
    }

    @Test
    void getLoanQuote_haveMarketData_loanQuoteWithSameLoanAmountAndRate() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(3600));
        var calculator = spy(new LoanQuotePerLenderCalculator(marketData1));

        var quote = calculator.getLoanQuote();

        assertThat(quote, notNullValue());
        assertThat(quote.getLoanAmount(), is(BigInteger.valueOf(3600)));
        assertThat(quote.getAverageRate(), is(BigDecimal.valueOf(0.1)));
    }

    @Test
    void getLoanQuote_methodsOfCalculation_calculatingFromMonthlyRepayment() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(500));
        var calculator = spy(new LoanQuotePerLenderCalculator(marketData1));
        doReturn(BigDecimal.ONE).when(calculator).getMonthlyRepayment(any(), any(), anyInt());

        calculator.getLoanQuote();

        var monthlyRate = BigDecimal.valueOf(0.1).setScale(14, RoundingMode.UP)
                .divide(BigDecimal.valueOf(12), RoundingMode.UP);
        verify(calculator).getMonthlyRepayment(eq(monthlyRate), eq(BigInteger.valueOf(500)), anyInt());
    }

    @Test
    void getLoanQuote_haveMarketData_36repayments() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(3600));
        var calculator = spy(new LoanQuotePerLenderCalculator(marketData1));
        doReturn(BigDecimal.valueOf(10)).when(calculator).getMonthlyRepayment(any(), any(), anyInt());

        var quote = calculator.getLoanQuote();

        var monthlyRate = BigDecimal.valueOf(0.1).setScale(14, RoundingMode.UP)
                .divide(BigDecimal.valueOf(12), RoundingMode.UP);
        verify(calculator).getMonthlyRepayment(eq(monthlyRate), eq(BigInteger.valueOf(3600)), eq(36));
        assertThat(quote, notNullValue());
        assertThat(quote.getMonthlyRepayment(), is(BigDecimal.valueOf(10)));
    }

    @Test
    void getLoanQuote_haveMarketData_monthlyRepaymentGivesCorrectTotalAmount() {
        var marketData1 = new MarketData("lender1", BigDecimal.valueOf(0.1), BigInteger.valueOf(3600));
        var calculator = spy(new LoanQuotePerLenderCalculator(marketData1));
        doReturn(BigDecimal.valueOf(20)).when(calculator).getMonthlyRepayment(any(), any(), anyInt());

        var quote = calculator.getLoanQuote();

        assertThat(quote, notNullValue());
        // Monthly repayment * Repayment Months = 20 * 36 = 720
        assertThat(quote.getTotalRepayment(), is(BigDecimal.valueOf(720)));
    }

    @Test
    void getMonthlyRepayment_positiveRate_formulaCorrect() {
        var calculator = new LoanQuotePerLenderCalculator(mock(MarketData.class));

        var monthlyRepayment = calculator.getMonthlyRepayment(BigDecimal.valueOf(0.1), BigInteger.valueOf(10000),
                10);

        // Formula is (r*P*((1+r)^N))/(((1+r)^N)-1) = (0.1*10000*((1+0.1)^10))/(((1+0.1)^10)-1) = 1627.45394883
        assertThat(monthlyRepayment, closeTo(BigDecimal.valueOf(1627.45394883), BigDecimal.valueOf(0.000001)));
    }

    @Test
    void getMonthlyRepayment_zeroRate_differentFormulaResultThanPositiveRate() {
        var calculator = new LoanQuotePerLenderCalculator(mock(MarketData.class));

        var monthlyRepayment = calculator.getMonthlyRepayment(BigDecimal.ZERO, BigInteger.valueOf(7),
                3);

        // Formula is P/N = 7/3 = 2.33333333333(3)
        assertThat(monthlyRepayment, closeTo(BigDecimal.valueOf(2.3333333333), BigDecimal.valueOf(0.000001)));
    }
}
