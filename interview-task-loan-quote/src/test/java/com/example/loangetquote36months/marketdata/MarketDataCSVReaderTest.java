package com.example.loangetquote36months.marketdata;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MarketDataCSVReaderTest {
    private PrintStream standardOut;

    private final ByteArrayOutputStream standardOutOutputStream = new ByteArrayOutputStream();

    @BeforeEach
    public void setup() {
        standardOut = System.out;

        System.setOut(new PrintStream(standardOutOutputStream));
    }

    @AfterEach
    public void teardown() {
        System.setOut(standardOut);
    }

    @Test
    public void constructor_nullFile_throwsNPEWithMessage() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new MarketDataCSVReader(null), "CSV file cannot be null");
    }

    @Test
    public void read_fileNotExisting_throwsMarketDataExceptionWithIOExceptionCause() throws Exception {
        var file = Files.createTempFile("MarketDataCSVReaderTest", "");
        assertThat(file.toFile().delete(), is(true));
        var reader = spy(new MarketDataCSVReader(file.toString()));
        doReturn(List.of()).when(reader).parseCSV(any());
        var msg = String.format("Unable to read market data CSV file [%s]", file.toString());

        var ex = Assertions.assertThrows(MarketDataException.class,
                reader::read, msg);

        assertThat(ex.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void read_fileExisting_parsingCSV() throws Exception {
        var file = Files.createTempFile("MarketDataCSVReaderTest", "");
        file.toFile().deleteOnExit();
        var reader = spy(new MarketDataCSVReader(file.toString()));
        doReturn(List.of()).when(reader).parseCSV(any());

        reader.read();

        verify(reader).parseCSV(any());
    }

    @Test
    public void read_noCSVRecords_notNull() throws Exception {
        var file = Files.createTempFile("MarketDataCSVReaderTest", "");
        file.toFile().deleteOnExit();
        var reader = spy(new MarketDataCSVReader(file.toString()));
        doReturn(List.of()).when(reader).parseCSV(any());

        var marketDataList = reader.read();

        assertThat(marketDataList, notNullValue());
    }

    @Test
    public void read_throwsNPEDuringParsing_reThrowsMarketDataException() throws Exception {
        var file = Files.createTempFile("MarketDataCSVReaderTest", "");
        file.toFile().deleteOnExit();
        var reader = spy(new MarketDataCSVReader(file.toString()));
        doThrow(new NullPointerException()).when(reader).parseCSV(any());

        var exception = Assertions.assertThrows(MarketDataException.class, reader::read);

        assertThat(exception.getCause(), notNullValue());
        assertThat(exception.getCause(), instanceOf(NullPointerException.class));
    }

    @Test
    public void parseCSV_noRecords_notNull() throws Exception {
        var reader = new MarketDataCSVReader("test");

        var marketDataList = reader.parseCSV(Stream.empty());

        assertThat(marketDataList, notNullValue());
    }

    @Test
    public void parseCSV_twoColumns_expectingThreeThrowsException() throws Exception {
        var record1 = List.of("val1", "val2");
        var reader = new MarketDataCSVReader("test");

        var msg = String.format("Invalid number of columns for record [%s]", record1);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () ->
                reader.parseCSV(Stream.of(record1)), msg);
    }

    @Test
    public void parseCSV_firstRecordNull_throwsException() throws Exception {
        var record1 = Stream.of(null, "val2", "val3").collect(Collectors.toList());
        var reader = new MarketDataCSVReader("test");

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                reader.parseCSV(Stream.of(record1)), "Lender cannot be empty for record [null]");
    }

    // TODO
    //  parseCSV fail with MarketDataException when 1st record is empty
    //  parseCSV fail with MarketDataException when 2nd record is missing (null)
    //  parseCSV fail with MarketDataException when 2nd record when not a number
    //  parseCSV fail with MarketDataException when 2nd record when below 0
    //  parseCSV fail with MarketDataException when 2nd record when above 1
    //  parseCSV fail with MarketDataException when 3rd record is missing (null)
    //  parseCSV fail with MarketDataException when 3rd record when not a number
    //  parseCSV fail with MarketDataException when 3rd record when below 0
    //  parseCSV fail with MarketDataException when 3rd record when equalTo 0
    //  parseCSV few records, returned correctly (numbers with some precision)
}
