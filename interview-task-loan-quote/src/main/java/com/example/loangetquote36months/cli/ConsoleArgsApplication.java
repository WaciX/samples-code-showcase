package com.example.loangetquote36months.cli;

import com.example.loangetquote36months.loanquote.BestLendersPicker;
import com.example.loangetquote36months.loanquote.LoanQuote;
import com.example.loangetquote36months.loanquote.LoanQuoteCalculator;
import com.example.loangetquote36months.loanquote.LoanQuoteConsolePrinter;
import com.example.loangetquote36months.marketdata.MarketData;
import com.example.loangetquote36months.marketdata.MarketDataCSVReader;
import org.springframework.boot.CommandLineRunner;

import java.math.BigInteger;
import java.util.List;

public class ConsoleArgsApplication implements CommandLineRunner {
    static final String HELP =
            "Usage: java -jar get-quote-for-loan-on-36-months-period-0.0.1-SNAPSHOT.jar [MARKET-CSV-FILE] [LOAN-AMOUNT]\n" +
                    "\tMARKET-CSV-FILE is 3 columns CSV format file that contains data about Lender, Rate, Available amount." +
                    "\tLOAN-AMOUNT is an amount between 1000 and 15000. Load amount will rounded to nearest (up) 100.";

    @Override
    public void run(String... args) throws Exception {
        if (args.length != 2) {
            System.out.println(HELP);
            return;
        }

        var loanAmount = new BigInteger(args[1]);
        if (loanAmount.intValue() < 1_000 || loanAmount.intValue() > 15_000) {
            throw new NumberFormatException("Loan amount needs to be in range [1000-15000]");
        }

        var reader = new MarketDataCSVReader(args[0]);
        var marketDataList = reader.read();

        var bestLendersPicker = new BestLendersPicker(marketDataList, loanAmount);
        List<MarketData> bestMarketDataDeals = bestLendersPicker.getBestDeals();
        if (bestMarketDataDeals.isEmpty()) {
            System.out.println("No deals available at the time");
            return;
        }

        var loanQuoteCalculator = new LoanQuoteCalculator(bestMarketDataDeals);
        var loanQuote = loanQuoteCalculator.getQuote();

        if (loanQuote.isPresent()) {
            var printer = new LoanQuoteConsolePrinter(loanQuote.get());
            printer.print();
        }
    }
}
