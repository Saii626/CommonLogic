package app.saikat.ThreadManagement.StatsLoggerInstanceTest;

import java.time.Instant;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.ThreadManagement.Stats;

public class A {

    private static int c = 0;

    private int counter = 0;
    private String str = "not set";

    @Inject
    public A() {
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Stats(rate = 1)
    private void printStatistics(Logger statsLogger) {
        ++counter;
        ++c;
        statsLogger.printf(Level.INFO, "A={name=%s, staticCounter:%d, instanceCounter:%d, currTime:%d}", str, c,
                counter, Instant.now()
                        .toEpochMilli());
    }

    public int getCounter() {
        return counter;
    }

    public static int getC() {
        return c;
    }
}