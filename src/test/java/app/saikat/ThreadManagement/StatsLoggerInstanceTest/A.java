package app.saikat.ThreadManagement.StatsLoggerInstanceTest;

import java.time.Instant;

import javax.inject.Inject;

import app.saikat.Annotations.ThreadManagement.Stats;

public class A {

    private static int c = 0;

    private int counter = 0;
    private String str = "not set";

    @SuppressWarnings("unused")
    public static class A_Stat {
        private String str;
        private int counter;
        private long timestamp;

        public A_Stat(int counter, String str) {
            this.counter = counter;
            this.timestamp = Instant.now()
                    .toEpochMilli();
            this.str = str;
        }
    }

    @Inject
    public A() {
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Stats(rate = 1)
    private A_Stat getAStatistics() {
        ++counter;
        ++c;
        return new A_Stat(counter, str);
    }

    public int getCounter() {
        return counter;
    }

    public static int getC() {
        return c;
    }
}