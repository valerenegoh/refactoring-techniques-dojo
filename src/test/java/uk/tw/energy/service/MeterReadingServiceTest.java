package uk.tw.energy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class MeterReadingServiceTest {

    private MeterService meterService;

    @BeforeEach
    public void setUp() {
        meterService = new MeterService(new HashMap<>());
    }

    @Test
    public void givenMeterIdThatDoesNotExistShouldReturnNull() {
        assertThat(meterService.getReadings("unknown-id"), is(Optional.empty()));
    }

    @Test
    public void givenMeterReadingThatExistsShouldReturnMeterReadings() {
        meterService.storeReadings("random-id", new ArrayList<>());
        assertThat(meterService.getReadings("random-id"), is(Optional.of(new ArrayList<>())));
    }
}
