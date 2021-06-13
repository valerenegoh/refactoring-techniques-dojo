package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.Instant;

public class ElectricityReading {

    private Instant time;
    private BigDecimal readingInKW;

    public ElectricityReading(Instant time, BigDecimal reading) {
        this.time = time;
        this.readingInKW = reading;
    }

    public BigDecimal getReadingInKW() {
        return readingInKW;
    }

    public Instant getTime() {
        return time;
    }
}
