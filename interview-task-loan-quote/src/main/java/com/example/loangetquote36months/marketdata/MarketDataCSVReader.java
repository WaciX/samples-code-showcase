package com.example.loangetquote36months.marketdata;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads through Market Data CSV file and returns in OO format.
 * @see MarketData
 */
public class MarketDataCSVReader {
    private final String filePath;

    public MarketDataCSVReader(String filePath) {
        Objects.requireNonNull(filePath, "CSV file cannot be null");

        this.filePath = filePath;
    }

    public List<MarketData> read() throws MarketDataException {
        try (var reader = Files.newBufferedReader(Paths.get(filePath));
             var csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            var stream = StreamSupport.stream(csvParser.spliterator(), false)
                    .map(CSVRecord::iterator)
                    .map(it -> {
                        List<String> list = new ArrayList<>();
                        it.forEachRemaining(list::add);
                        return list;
                    });
            return parseCSV(stream);
        } catch (Exception e) {
            var msg = String.format("Unable to read market data CSV file [%s]", filePath);
            throw new MarketDataException(msg, e);
        }
    }

    // @VisibleForTesting
    List<MarketData> parseCSV(Stream<List<String>> csvRecordStream) {
        // TODO There are probably better ways to validate that :)
        return csvRecordStream.peek(record -> {
            if (record.size() != 3) {
                throw new IndexOutOfBoundsException(
                        String.format("Invalid number of columns for record [%s]", record));
            }
        }).map(record -> {
            var lender = record.get(0);
            if (StringUtils.isEmpty(lender)) {
                throw new IllegalArgumentException(
                        String.format("Lender cannot be empty for record [%s]", record));
            }

            // TODO should catch exception from BigDecimal and BigInteger and add record info to it
            var rate = new BigDecimal(record.get(1));
            if (rate.doubleValue() < 0 || rate.doubleValue() > 1) {
                throw new NumberFormatException(
                        String.format("Rate needs to be within range of [0-1] for record [%s]", record));
            }

            var amountToLend = new BigInteger(record.get(2));
            if (amountToLend.intValue() <= 0) {
                throw new NumberFormatException(
                        String.format("Amount to Lend needs to be positive for record [%s]", record));
            }

            return new MarketData(lender, rate, amountToLend);
        }).collect(Collectors.toList());
    }
}
