package com.example.loangetquote36months.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public class ConsoleArgsApplicationTest {

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
    void run_emptyArgs_printsHelpOnStdOutAndExit() throws Exception {
        var app = new ConsoleArgsApplication();

        app.run();

        assertThat(standardOutOutputStream.toString(), is(ConsoleArgsApplication.HELP+System.lineSeparator()));
    }

    @Test
    void run_oneArg_printsHelpOnStdOutAndExit() throws Exception {
        var app = new ConsoleArgsApplication();

        app.run("afile");

        assertThat(standardOutOutputStream.toString(), is(ConsoleArgsApplication.HELP+System.lineSeparator()));
    }

    @Test
    void run_notANumberAmount_exitsWithException() {
        var app = new ConsoleArgsApplication();

        Assertions.assertThrows(NumberFormatException.class, () ->
                app.run("afile", "notanumber"));
    }

    @Test
    void run_amountBelow1000_exitsWithException() {
        var app = new ConsoleArgsApplication();

        Assertions.assertThrows(NumberFormatException.class, () ->
                app.run("afile", "999"), "Loan amount needs to be in range [1000-15000]");
    }

    @Test
    void run_amountAbove15000_exitsWithException() {
        var app = new ConsoleArgsApplication();

        Assertions.assertThrows(NumberFormatException.class, () ->
                app.run("afile", "15001"), "Loan amount needs to be in range [1000-15000]");
    }

    @Test
    void run_amountEqual1000_noException() throws Exception {
        var file = File.createTempFile("run_noMarketData_printsNoDealsExits", "b");
        file.deleteOnExit();

        var app = new ConsoleArgsApplication();

        app.run(file.getAbsolutePath(), "1000");
    }

    @Test
    void run_amountEqual15000_noException() throws Exception {
        var file = File.createTempFile("run_noMarketData_printsNoDealsExits", "b");
        file.deleteOnExit();

        var app = new ConsoleArgsApplication();

        app.run(file.getAbsolutePath(), "15000");
    }

    @Test
    void run_noMarketData_printsNoDealsExits() throws Exception {
        var file = File.createTempFile("run_noMarketData_printsNoDealsExits", "b");
        file.deleteOnExit();

        var app = new ConsoleArgsApplication();

        app.run(file.getAbsolutePath(), "5000");

        assertThat(standardOutOutputStream.toString(), is("No deals available at the time"+System.lineSeparator()));
    }
}
